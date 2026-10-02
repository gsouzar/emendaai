package br.senai.sp.emendaai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.senai.sp.emendaai.R;
import br.senai.sp.emendaai.model.Feriado;
import br.senai.sp.emendaai.util.Datas;

public class FeriadoAdapter extends RecyclerView.Adapter<FeriadoAdapter.FeriadoHolder> {

    public interface OnFeriadoClickListener {
        void aoClicar(Feriado feriado);
    }

    private final List<Feriado> feriados = new ArrayList<>();
    private final OnFeriadoClickListener listener;

    public FeriadoAdapter(OnFeriadoClickListener listener) {
        this.listener = listener;
    }

    public void atualizar(List<Feriado> novos) {
        feriados.clear();
        feriados.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeriadoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feriado, parent, false);
        return new FeriadoHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeriadoHolder holder, int position) {
        Feriado f = feriados.get(position);
        holder.txtDia.setText(Datas.dia(f.getData()));
        holder.txtMes.setText(Datas.mesCurto(f.getData()));
        holder.txtNome.setText(f.getNome());
        holder.txtSemana.setText(Datas.diaDaSemana(f.getData()));
        holder.txtContagem.setText(Datas.contagem(f.getData()));
        holder.txtSelo.setVisibility(Datas.ehEmenda(f.getData()) ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.aoClicar(f);
            }
        });
    }

    @Override
    public int getItemCount() {
        return feriados.size();
    }

    static class FeriadoHolder extends RecyclerView.ViewHolder {
        final TextView txtDia, txtMes, txtNome, txtSemana, txtContagem, txtSelo;

        FeriadoHolder(@NonNull View itemView) {
            super(itemView);
            txtDia = itemView.findViewById(R.id.txtDia);
            txtMes = itemView.findViewById(R.id.txtMes);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtSemana = itemView.findViewById(R.id.txtSemana);
            txtContagem = itemView.findViewById(R.id.txtContagem);
            txtSelo = itemView.findViewById(R.id.txtSelo);
        }
    }
}