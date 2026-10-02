package br.senai.sp.emendaai.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Uma instância de Retrofit para o app inteiro. */
public final class ApiClient {

    // A barra final é obrigatória: sem ela o Retrofit lança IllegalArgumentException.
    private static final String BASE_URL = "https://brasilapi.com.br/api/";

    private static Retrofit retrofit;
    private static BrasilApiService servico;

    private ApiClient() {
    }

    public static synchronized BrasilApiService getService() {
        if (servico == null) {
            // Imprime URL e código HTTP no Logcat (filtre por "OkHttp").
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient cliente = new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(cliente)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            servico = retrofit.create(BrasilApiService.class);
        }
        return servico;
    }
}
