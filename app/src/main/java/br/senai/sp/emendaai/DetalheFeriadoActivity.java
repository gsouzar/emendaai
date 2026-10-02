package br.senai.sp.emendaai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import br.senai.sp.emendaai.api.ApiClient;
import br.senai.sp.emendaai.model.Endereco;
import br.senai.sp.emendaai.util.Datas;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetalheFeriadoActivity extends AppCompatActivity {

    private static final String TAG = "EmendaAi";
    private static final String CHAVE_ENDERECO = "endereco_encontrado";

    private EditText campoCep;
    private ProgressBar progressoCep;
    private TextView txtResultadoCep;
    private Button btnBuscarCep;
    private Button btnCompartilhar;

    private String nomeFeriado;
    private String dataFeriado;
    private String enderecoEncontrado;
    private Call<Endereco> chamadaCep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_feriado);

        // Recebe o Bundle enviado pela MainActivity.
        Bundle dados = getIntent().getExtras();
        if (dados == null) {
            finish();
            return;
        }
        nomeFeriado = dados.getString(MainActivity.EXTRA_NOME);
        dataFeriado = dados.getString(MainActivity.EXTRA_DATA);

        TextView txtNome = findViewById(R.id.txtNomeFeriado);
        TextView txtData = findViewById(R.id.txtDataFeriado);
        TextView txtContagem = findViewById(R.id.txtContagemFeriado);
        TextView txtEmenda = findViewById(R.id.txtAvisoEmenda);

        campoCep = findViewById(R.id.campoCep);
        progressoCep = findViewById(R.id.progressoCep);
        txtResultadoCep = findViewById(R.id.txtResultadoCep);
        btnBuscarCep = findViewById(R.id.btnBuscarCep);
        btnCompartilhar = findViewById(R.id.btnCompartilhar);

        txtNome.setText(nomeFeriado);
        txtData.setText(Datas.porExtenso(dataFeriado) + " · "
                + Datas.diaDaSemana(dataFeriado));
        txtContagem.setText(Datas.contagem(dataFeriado));
        txtEmenda.setVisibility(Datas.ehEmenda(dataFeriado) ? View.VISIBLE : View.GONE);

        btnBuscarCep.setOnClickListener(v -> buscarCep());
        btnCompartilhar.setOnClickListener(v -> compartilhar());

        if (savedInstanceState != null) {
            String salvo = savedInstanceState.getString(CHAVE_ENDERECO);
            if (salvo != null) {
                enderecoEncontrado = salvo;
                mostrarEndereco(salvo);
            }
        }
    }

    private void buscarCep() {
        // Aceita "01001-000" também: tira tudo que não é dígito.
        String cep = campoCep.getText().toString().replaceAll("\\D", "");

        // Valide antes de gastar rede.
        if (cep.length() != 8) {
            mostrarMensagem(getString(R.string.erro_cep_curto));
            return;
        }

        if (chamadaCep != null) {
            chamadaCep.cancel();
        }

        enderecoEncontrado = null;
        btnCompartilhar.setEnabled(false);
        btnBuscarCep.setEnabled(false);
        progressoCep.setVisibility(View.VISIBLE);
        mostrarMensagem(getString(R.string.msg_buscando_cep));

        chamadaCep = ApiClient.getService().buscarCep(cep);
        chamadaCep.enqueue(new Callback<Endereco>() {
            @Override
            public void onResponse(@NonNull Call<Endereco> call,
                                   @NonNull Response<Endereco> resposta) {
                if (call.isCanceled()) {
                    return;
                }
                terminarBusca();

                // Mensagem específica primeiro: o 404 é culpa do dado digitado.
                if (resposta.code() == 404) {
                    mostrarMensagem(getString(R.string.erro_cep_inexistente));
                    return;
                }
                if (!resposta.isSuccessful()) {
                    mostrarMensagem(getString(R.string.msg_erro_servidor, resposta.code()));
                    return;
                }

                Endereco endereco = resposta.body();
                if (endereco == null) {
                    mostrarMensagem(getString(R.string.erro_cep_inexistente));
                    return;
                }

                enderecoEncontrado = endereco.paraTexto();
                mostrarEndereco(enderecoEncontrado);
            }

            @Override
            public void onFailure(@NonNull Call<Endereco> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                Log.e(TAG, "Falha ao buscar CEP", t);
                terminarBusca();
                mostrarMensagem(getString(R.string.msg_erro_rede));
            }
        });
    }

    private void terminarBusca() {
        progressoCep.setVisibility(View.GONE);
        btnBuscarCep.setEnabled(true);
    }

    private void mostrarMensagem(String mensagem) {
        txtResultadoCep.setText(mensagem);
        txtResultadoCep.setVisibility(View.VISIBLE);
        btnCompartilhar.setEnabled(false);
    }

    private void mostrarEndereco(String endereco) {
        txtResultadoCep.setText(endereco);
        txtResultadoCep.setVisibility(View.VISIBLE);
        btnCompartilhar.setEnabled(true);
    }

    private void compartilhar() {
        if (enderecoEncontrado == null) {
            return;
        }
        String texto = getString(R.string.texto_compartilhar,
                nomeFeriado,
                Datas.porExtenso(dataFeriado),
                Datas.diaDaSemana(dataFeriado),
                enderecoEncontrado);

        Intent envio = new Intent(Intent.ACTION_SEND);
        envio.setType("text/plain");
        envio.putExtra(Intent.EXTRA_TEXT, texto);
        startActivity(Intent.createChooser(envio, getString(R.string.btn_compartilhar)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CHAVE_ENDERECO, enderecoEncontrado);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chamadaCep != null) {
            chamadaCep.cancel();
        }
    }
}