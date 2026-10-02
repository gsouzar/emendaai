package br.senai.sp.emendaai.api;

import java.util.List;

import br.senai.sp.emendaai.model.Endereco;
import br.senai.sp.emendaai.model.Feriado;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** O contrato inteiro com a BrasilAPI. O Retrofit escreve a implementação. */
public interface BrasilApiService {

    @GET("feriados/v1/{ano}")
    Call<List<Feriado>> listarFeriados(@Path("ano") int ano);

    @GET("cep/v2/{cep}")
    Call<Endereco> buscarCep(@Path("cep") String cep);
}
