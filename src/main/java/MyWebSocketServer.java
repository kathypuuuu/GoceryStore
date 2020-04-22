import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MyWebSocketServer extends WebSocketServer {

//    private static int TCP_PORT = 9090;

    private Set<WebSocket> conns;
    public static ThriftHandler handler;

    public MyWebSocketServer(int socketPortNumber, ThriftHandler handler) {
        super(new InetSocketAddress(socketPortNumber));
        conns = new HashSet<WebSocket>();
        this.handler = handler;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conns.add(conn);
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
        System.out.println("One connection closed. " + conns.size() + " connections remain open. ");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        UserRequest request = null;
        ProductRequest pRequest = null;
        StockRequest sRequest = null;
        String ret = null;
        System.out.println("Message from client: " + message + " " + Thread.currentThread());
        //TODO: Thrift: notify all servers
        //TODO: Commit to database
        //TODO: send message back
        JSONObject clientRequest = null;
        try {
            clientRequest = (JSONObject) new JSONParser().parse(message);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String type = (String) clientRequest.get("type");

        System.out.println(type + " type");
        JSONObject json = new JSONObject();

        switch (type) {
//            case Constant.STOCK_TYPE:
//
//                json.put("status", 200);
//                json.putAll(INITIAL_PRODUCT);
//                System.out.println(json.toString());
//                break;
//            case Constant.REGISTER_TYPE:
//                json.put("status", 401);
//                break;
            case Constant.REGISTER:
                request = new UserRequest((String) clientRequest.get("type"), (String) clientRequest.get("username"),
                        (String) clientRequest.get("password"), (String) clientRequest.get("identity"));
                ret = handler.updateRequestFromClient(request);
                System.out.println(ret);
                if (ret.equals("Register succeed")) {
                    json.put("status", 200);
                } else {
                    json.put("status", 401);
                }
                ret = json.toString();
                break;
            case Constant.LOGIN:
                request = new UserRequest((String) clientRequest.get("type"), (String) clientRequest.get("username"),
                        (String) clientRequest.get("password"), (String) clientRequest.get("identity"));
                ret = handler.updateRequestFromClient(request);
                System.out.println(ret);
                if (ret.equals("Login success")) {
                    json.put("status", 200);
                } else {
                    json.put("status", 401);
                }
                ret = json.toString();
                break;
            case Constant.BUY:
            case Constant.SUPPLEMENT:
                pRequest = new ProductRequest((String) clientRequest.get("type"),(String) clientRequest.get("product"),
                        (String) clientRequest.get("number"));
                System.out.println("itenname is "+pRequest.itemName);
                ret = handler.updateProductRequestFromClient(pRequest);
                System.out.println("res get from update "+ret);
                if (ret.equals("Update product success")) {
                    json.put("status", 200);
                } else {
                    json.put("status", 401);
                }
                ret = json.toString();
                break;
            case Constant.STOCKS:
                    sRequest = new StockRequest((String) clientRequest.get("type"));
                    ret = handler.updateStockRequestFromClient(sRequest);
                    System.out.println("res get from update "+ret);
                    break;
            default:
                json.put("status", 200);
        }
        conn.send(ret);
        System.out.println("Response to Client:  " + json.toString());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            conns.remove(conn);
        }
    }


//    public static void main(String[] args) {
//
//
//        new MyWebSocketServer().run();
//    }
}