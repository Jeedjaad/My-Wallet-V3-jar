package info.blockchain.wallet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockInterceptor implements Interceptor {

    private static Logger log = LoggerFactory.getLogger(MockInterceptor.class);

    static MockInterceptor instance;

    LinkedList<Pair<Integer, String>> responseList;
    boolean ioException = false;

    private MockInterceptor() {
    }

    public static MockInterceptor getInstance() {

        if(instance == null){
            instance = new MockInterceptor();
        }
        return instance;
    }

    public void setIOException(boolean throwException){
        ioException = throwException;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        if(ioException)
            throw new IOException();

        final HttpUrl uri = chain.request().url();
        final String query = uri.query();
        final String method = chain.request().method();

        String responseString;
        int responseCode;

        if(!responseList.isEmpty()) {
            responseString = responseList.getFirst().getRight();
            responseCode = responseList.getFirst().getLeft();
        } else {
            responseString = "{}";
            responseCode = 200;
        }

        Response response = new Response.Builder()
                .code(responseCode)
                .message(responseString)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), responseString.getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        //Reset responses
        if(!responseList.isEmpty()) {
            responseList.removeFirst();
        }

        ioException = false;

        return response;
    }

    public void setResponse(Integer code, String response) {

        responseList = new LinkedList<>();
        responseList.add(Pair.of(code, response));
    }

    public void setResponseList(LinkedList<Pair> responseList) {
        this.responseList = new LinkedList<>();
        for(Pair pair : responseList) {
            this.responseList.add(Pair.of((Integer) pair.getLeft(), (String)pair.getRight()));
        }
    }

    @Deprecated
    public void setResponseString(String response) {
        setResponse(200, response);
    }
}
