package br.senai.sp.emendaai.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/** Um item do array devolvido por GET /api/feriados/v1/{ano}. */
public class Feriado implements Serializable {

    @SerializedName("date")
    private String data;

    @SerializedName("name")
    private String nome;

    @SerializedName("type")
    private String tipo;

    public String getData() {
        return data;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }
}
