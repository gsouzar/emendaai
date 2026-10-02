package br.senai.sp.emendaai.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/** Objeto devolvido por GET /api/cep/v2/{cep}. */
public class Endereco implements Serializable {

    @SerializedName("cep")
    private String cep;

    @SerializedName("state")
    private String uf;

    @SerializedName("city")
    private String cidade;

    @SerializedName("neighborhood")
    private String bairro;

    @SerializedName("street")
    private String rua;

    public String getCep() {
        return cep;
    }

    public String getUf() {
        return uf;
    }

    public String getCidade() {
        return cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public String getRua() {
        return rua;
    }

    /** CEPs gerais de cidade pequena vêm sem rua e sem bairro, então cada parte é opcional. */
    public String paraTexto() {
        StringBuilder sb = new StringBuilder();
        if (rua != null && !rua.isEmpty()) {
            sb.append(rua).append('\n');
        }
        if (bairro != null && !bairro.isEmpty()) {
            sb.append(bairro).append('\n');
        }
        if (cidade != null) {
            sb.append(cidade);
        }
        if (uf != null) {
            sb.append(" - ").append(uf);
        }
        if (cep != null) {
            sb.append('\n').append("CEP ").append(cep);
        }
        return sb.toString().trim();
    }
}
