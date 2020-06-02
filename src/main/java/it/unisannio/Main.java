package it.unisannio;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args){
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpServer server = HttpServer.createSimpleServer("/", "0.0.0.0", 8079);
        server.getServerConfiguration().addHttpHandler(new HttpHandler() {
            public void service(Request request, Response response) throws Exception {
                String requestURI = request.getRequestURI();
                System.out.println(requestURI);
                String endpoint = getEndpoint(requestURI);
                System.out.println(endpoint);
                String uri = getURI(requestURI);
                System.out.println(uri);
                Method method = request.getMethod();
                System.out.println(method.getMethodString());
                HttpRequest.Builder builder = HttpRequest.newBuilder();
                switch (method.getMethodString()){
                    case "GET":
                        builder.GET();
                    break;
                    case "POST":
                        builder.POST(HttpRequest.BodyPublishers.ofString(request.getPostBody(request.getContentLength()).toStringContent()));
                    break;
                }
                builder.uri(URI.create(endpoint+uri+addPathParams(request)));

                for(String name: request.getHeaderNames())
                    if(!(name.equals("host") || name.equals("connection") || name.equals("content-length")))
                        builder.setHeader(name,request.getHeader(name));

                HttpRequest r = builder.build();

                HttpResponse<String> re = httpClient.send(r, HttpResponse.BodyHandlers.ofString());

                response.setStatus(re.statusCode());
                Set<String> headersFields = re.headers().map().keySet();
                for(String field: headersFields){
                    List<String> values = re.headers().map().get(field);
                    for(String value: values)
                        response.setHeader(field,value);
                }
                response.getWriter().write(re.body());
            }
        }, "/api-v1/bank");
        try {
            server.start();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private static String[] tokenizeRequestURI(String requestURI){
        requestURI = requestURI.replaceAll("/$|^/", "");
        return requestURI.split("/");
    }

    private static String getEndpoint(String requestURI) {
        String[] tokens = tokenizeRequestURI(requestURI);
        String endpoint = null;
        System.out.println(tokens[2]);
        switch (tokens[2]){
            case "accounts":
                endpoint = "http://" + System.getenv("ACCOUNT_HOST") + ":8080/Account-1.0/";
                //endpoint = "http://" + "localhost" + ":8080/Account-1.0/";
            break;
            case "customers":
                endpoint = "http://" + System.getenv("CUSTOMER_HOST") + ":8080/Customer-1.0/";
                //endpoint = "http://" + "localhost" + ":8080/Customer-1.0/";
            break;
        }
        return endpoint;
    }

    private static String getURI(String requestURI) {
        String[] tokens = tokenizeRequestURI(requestURI);
        StringBuilder URI = new StringBuilder();
        for(int i = 0; i < tokens.length; i++)
            if (i != 1)
                URI.append(tokens[i]+"/");
        return URI.toString();
    }

    private static String addPathParams(Request request){
        if(request.getParameterMap().isEmpty())
            return "";
        StringBuilder params = new StringBuilder();
        params.append("?");
        Set<String> paramNames = request.getParameterMap().keySet();
        for(String name: paramNames) {
            String[] values = request.getParameterMap().get(name);
            for (int i= 0; i < values.length; i++ )
                params.append(name+"="+values[i]);
        }
        System.out.println(params.toString());
        return params.toString();
    }
}
