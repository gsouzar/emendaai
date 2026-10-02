package br.senai.sp.emendaai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.senai.sp.emendaai.adapter.FeriadoAdapter;
import br.senai.sp.emendaai.api.ApiClient;
import br.senai.sp.emendaai.model.Feriado;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EmendaAi";
    private static final String CHAVE_ANO = "ano_selecionado";
    private static final String CHAVE_FERIADOS = "feriados_carregados";
    public static final String EXTRA_NOME = "nome_feriado";
    public static final String EXTRA_DATA = "data_feriado";
    public static final String EXTRA_TIPO = "tipo_feriado";

    /** Os quatro estados da tela. */
    private enum Estado {
        CARREGANDO, CONTEUDO, VAZIO, ERRO
    }

    private ProgressBar progresso;
    private RecyclerView lista;
    private View blocoErro;
    private TextView txtMensagemErro;
    private TextView txtVazio;

    private final Button[] botoesAno = new Button[3];

    private FeriadoAdapter adapter;
    private int anoSelecionado;
    private ArrayList<Feriado> feriados = new ArrayList<>();
    private Call<List<Feriado>> chamadaAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progresso = findViewById(R.id.progresso);
        lista = findViewById(R.id.listaFeriados);
        blocoErro = findViewById(R.id.blocoErro);
        txtMensagemErro = findViewById(R.id.txtMensagemErro);
        txtVazio = findViewById(R.id.txtVazio);

        botoesAno[0] = findViewById(R.id.btnAno1);
        botoesAno[1] = findViewById(R.id.btnAno2);
        botoesAno[2] = findViewById(R.id.btnAno3);

        // O adapter é criado agora, vazio, e recebe os dados quando a resposta chegar.
        adapter = new FeriadoAdapter(this::abrirDetalhe);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        int anoAtual = LocalDate.now().getYear();
        anoSelecionado = anoAtual;

        for (int i = 0; i < botoesAno.length; i++) {
            final int ano = anoAtual + i;
            botoesAno[i].setText(String.valueOf(ano));

            botoesAno[i].setOnClickListener(v -> {
                anoSelecionado = ano;
                marcarBotaoDoAno();
                buscarFeriados(ano);
            });
        }

        findViewById(R.id.btnTentarDeNovo).setOnClickListener(v -> buscarFeriados(anoSelecionado));

        // Girar o aparelho recria a Activity: se já temos dados salvos, não chama a rede de novo.
        if (savedInstanceState != null) {
            anoSelecionado = savedInstanceState.getInt(CHAVE_ANO, anoAtual);
            @SuppressWarnings("unchecked")
            ArrayList<Feriado> salvos =
                    (ArrayList<Feriado>) savedInstanceState.getSerializable(CHAVE_FERIADOS);
            if (salvos != null && !salvos.isEmpty()) {
                feriados = salvos;
                marcarBotaoDoAno();
                adapter.atualizar(feriados);
                mostrar(Estado.CONTEUDO);
                return;
            }
        }

        marcarBotaoDoAno();
        buscarFeriados(anoSelecionado);
    }

    private void buscarFeriados(int ano) {
        // Se o usuário trocar de ano com uma chamada em voo, a antiga é descartada.
        if (chamadaAtual != null) {
            chamadaAtual.cancel();
        }

        mostrar(Estado.CARREGANDO);

        chamadaAtual = ApiClient.getService().listarFeriados(ano);
        chamadaAtual.enqueue(new Callback<List<Feriado>>() {
            @Override
            public void onResponse(@NonNull Call<List<Feriado>> call,
                                   @NonNull Response<List<Feriado>> resposta) {
                if (call.isCanceled()) {
                    return;
                }

                // onResponse não quer dizer que deu certo: 404, 500, 429...
                if (!resposta.isSuccessful()) {
                    mostrarErro(getString(R.string.msg_erro_servidor, resposta.code()));
                    return;
                }

                List<Feriado> corpo = resposta.body();
                if (corpo == null || corpo.isEmpty()) {
                    feriados.clear();
                    mostrar(Estado.VAZIO);
                    return;
                }

                feriados = new ArrayList<>(corpo);
                adapter.atualizar(feriados);
                mostrar(Estado.CONTEUDO);
            }

            @Override
            public void onFailure(@NonNull Call<List<Feriado>> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                Log.e(TAG, "Falha ao buscar feriados", t);
                mostrarErro(getString(R.string.msg_erro_rede));
            }
        });
    }

    /** Clique no card: empacota os dados do feriado num Bundle e abre a tela de detalhe. */
    private void abrirDetalhe(Feriado feriado) {
        Bundle dados = new Bundle();
        dados.putString(EXTRA_NOME, feriado.getNome());
        dados.putString(EXTRA_DATA, feriado.getData());
        dados.putString(EXTRA_TIPO, feriado.getTipo());

        Intent intent = new Intent(this, DetalheFeriadoActivity.class);
        intent.putExtras(dados);
        startActivity(intent);
    }

    private void mostrarErro(String mensagem) {
        txtMensagemErro.setText(mensagem);
        mostrar(Estado.ERRO);
    }

    private void mostrar(Estado estado) {
        progresso.setVisibility(estado == Estado.CARREGANDO ? View.VISIBLE : View.GONE);
        lista.setVisibility(estado == Estado.CONTEUDO ? View.VISIBLE : View.GONE);
        txtVazio.setVisibility(estado == Estado.VAZIO ? View.VISIBLE : View.GONE);
        blocoErro.setVisibility(estado == Estado.ERRO ? View.VISIBLE : View.GONE);
    }

    private void marcarBotaoDoAno() {
        for (Button botao : botoesAno) {
            boolean escolhido = botao.getText().toString().equals(String.valueOf(anoSelecionado));
            botao.setAlpha(escolhido ? 1f : 0.45f);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CHAVE_ANO, anoSelecionado);
        outState.putSerializable(CHAVE_FERIADOS, feriados);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chamadaAtual != null) {
            chamadaAtual.cancel();
        }
    }
}