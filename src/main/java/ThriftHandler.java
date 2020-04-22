import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThriftHandler implements ThriftService.Iface {
    private final static String CUSTOMER_TYPE = "customer";
    private final static String MERCHANT_TYPE = "merchant";
    private final static String BUY_TYPE= "buy";
    private final static String SUPPLYMENT_TYPE = "supplement";
    private final static String STOCK_TYPE = "stocks";
    /**
     * map is the backend storage
     */
    private ConcurrentMap<String, List<String>> userMap;
    private ConcurrentHashMap<String,Integer> itemMap = new ConcurrentHashMap<>();

    /**
     * PortList represents the other port number other server listen to
     */
    private int[] portList;
    /**
     * Port number represents the port number this server listen to
     */
    private int portNumber;
    private boolean isReadyToUpdate;
    /**
     * constructor
     */
    public ThriftHandler(int portNumber, int[] portList) {
        this.userMap = new ConcurrentHashMap<>();
        this.portNumber = portNumber;
        this.portList = portList;
        this.isReadyToUpdate = true;
    }



    /**
     * This method is send doCommit request to the rest four servers
     * @param request the message that we want to send
     * @return true 4 doCommit, false 1 or more doAbort
     */
    private boolean twoPCUserRequest(UserRequest request) {
        for (int i = 0; i < portList.length; i++) {
            if (portList[i] == this.portNumber) {
                continue;
            }

            ThriftClient client = new ThriftClient(portList[i]);
            String ret = null;
            ret = client.sendUserRequest(request, ThriftClient.RequestStatus.DOCOMMIT);
            if (ret == null || ret.equals("ABORT")) {
                return false;
            }
        }
        return true;
    }

    private boolean twoPCProducRequest(ProductRequest request) {
        for (int i = 0; i < portList.length; i++) {
            if (portList[i] == this.portNumber) {
                continue;
            }

            ThriftClient client = new ThriftClient(portList[i]);
            String ret = null;
            ret = client.sendProductRequest(request, ThriftClient.RequestStatus.DOCOMMIT);
            if (ret == null || ret.equals("ABORT")) {
                return false;
            }
        }
        return true;
    }

    private boolean twoPCStockRequest(StockRequest request) {
        for (int i = 0; i < portList.length; i++) {
            if (portList[i] == this.portNumber) {
                continue;
            }

            ThriftClient client = new ThriftClient(portList[i]);
            String ret = null;
            ret = client.sendStockRequest(request, ThriftClient.RequestStatus.DOCOMMIT);
            if (ret == null || ret.equals("ABORT")) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is used to get key from local storage
     * @param key the key we want to get
     * @return the value
     */
    private String logIn(UserRequest request) {
        List<String> value = userMap.get(request.getUsername());
        String ret = null;
        if (value == null) {
            return "Password is wrong";
        } else if (!value.get(0).equals(request.getPassword()) || !value.get(1).equals(request.getIdentity())) {
            return "Password is wrong";
        } else {
            return "Login success";
        }
    }
    /**
     * This method is used to put key from local storage
     * @param k the key we want to put
     * @param v the value we want to put
     * @return the message to indicate the result of the operation
     */
    private String register(UserRequest request) {
        List<String> value = userMap.get(request.getUsername());
        if (value != null) {
            return "This user already exist";
        } else {
            value = new ArrayList<>();
            value.add(request.getPassword());
            value.add(request.getIdentity());
        }
        userMap.put(request.getUsername(), value);
        return "Register succeed";
    }

    public String checkUserType(ProductRequest request){
        System.out.println("enter check "+ request.itemName);
        String type = request.type;
        if(type.equals(BUY_TYPE)){

            if(itemMap.get(request.itemName) != null){
                int stocks = itemMap.get(request.itemName);
                if(stocks < Integer.valueOf(request.stock)) {

                    return "No enough stocks";
                }
                else itemMap.put(request.itemName, stocks - Integer.valueOf(request.stock));

                return "Update product success";
            }

            return "No such item!";
        }
        else if(type.equals(SUPPLYMENT_TYPE)){
            if(itemMap.get(request.itemName) != null) {
                int stocks = itemMap.get(request.itemName);
                itemMap.put(request.itemName, stocks + Integer.valueOf(request.stock));
            }
            else{
                itemMap.put(request.itemName, Integer.valueOf(request.stock));
                System.out.println(itemMap.toString());
            }
            return "Update product success";

        }

        return "Invalid type input,try again!";

    }

    public String checkStocks(StockRequest request){
        String type = request.type;
//        StringBuilder sb = new StringBuilder();
        JSONObject json = new JSONObject();

        if(type.equals(STOCK_TYPE)){
            json.putAll(itemMap);
            json.put("status",200);

            return json.toString();
        }
//        String res = sb.toString();
        json.put("status",401);
        return json.toString();

    }

    /**
     * This method is used to generate the log information with timestampe
     * @param message the message that we want to print to log
     */
    private void printConsole(String message) {
        Long currentTime = System.currentTimeMillis();
        SimpleDateFormat readableFormat = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultDate = new Date(currentTime);
        String newMessage = String.format("%s: %s", readableFormat.format(resultDate), message);
        System.out.println(newMessage);
    }

    /**
     * This method is to do the doCommit operation
     * @param request the message that we want to operate
     * @return if the server is running well, send COMMIT, otherwise ABORT
     */
    @Override
    public String doCommit() {
        printConsole(String.format("In server with port number %d. Can server process request?", this.portNumber));
        if (this.isReadyToUpdate) {
            // Server is good
            printConsole(String.format("Server with port number %d, send %s", this.portNumber, "COMMIT"));
            return "COMMIT";
        } else {
            // There is something wrong on the server
            printConsole(String.format("Server with port number %d, send %s", this.portNumber, "ABORT"));
            return "ABORT";
        }
    }

    /**
     * This method is to do the PUT/GET/DELETE operation
     * @param request the message that we want to operate
     * @return messaage to indicate the result of the operation
     */
    @Override
    public String updateRequestFromClient(UserRequest request) {
        printConsole("Server with port number " + this.portNumber +  " receive request: " + request);
        String ret = null;
        if (!twoPCUserRequest(request)) {
            ret = "GLOBAL_ABORT";
            printConsole(ret);
        } else {
            printConsole("GLOBAL_COMMIT");
            for (int i = 0; i < portList.length; i++) {
                if (portList[i] == this.portNumber) {
                    continue;
                }
                ThriftClient client = new ThriftClient(portList[i]);
                ret = client.sendUserRequest(request, ThriftClient.RequestStatus.SERVERUSERREQUEST);
                printConsole(ret);
            }
            if(request.getType().equals(Constant.LOGIN)) {
                ret = logIn(request);
            } else if(request.getType().equals(Constant.REGISTER)) {
                ret = register(request);
            }
        }
        return ret;
    }

    @Override
    public String updateRequestFromServer(UserRequest request) {
        String ret = null;
        if(request.getType().equals(Constant.LOGIN)) {
            ret = logIn(request);
        } else if(request.getType().equals(Constant.REGISTER)) {
            ret = register(request);
        }
        return ret;
    }

    @Override
    public String updateProductRequestFromClient(ProductRequest request) {
        printConsole("Server with port number " + this.portNumber +  " receive request: " + request);
        String ret = null;
        if (!twoPCProducRequest(request)) {
            ret = "GLOBAL_ABORT";
            printConsole(ret);
        } else {
            printConsole("GLOBAL_COMMIT");
            for (int i = 0; i < portList.length; i++) {
                if (portList[i] == this.portNumber) {
                    continue;
                }
                ThriftClient client = new ThriftClient(portList[i]);
                ret = client.sendProductRequest(request, ThriftClient.RequestStatus.SERVERPRODUCTREQUEST);
                printConsole(ret);
            }
            if(request.getType().equals(Constant.BUY) || request.getType().equals(Constant.SUPPLEMENT)) {
                ret = checkUserType(request);
            }
        }
        return ret;
    }

    @Override
    public String updateProductRequestFromServer(ProductRequest request) {
        String ret = null;
        if(request.getType().equals(Constant.BUY) || request.getType().equals(Constant.SUPPLEMENT)) {
            ret = checkUserType(request);
        }
        return ret;
    }

    @Override
    public String updateStockRequestFromClient(StockRequest request){
        System.out.println("******** enter stock update");
        printConsole("Server with port number " + this.portNumber +  " receive request: " + request);
        String ret = null;
        if (!twoPCStockRequest(request)) {
            ret = "GLOBAL_ABORT";
            printConsole(ret);
        } else {
            printConsole("GLOBAL_COMMIT");
            for (int i = 0; i < portList.length; i++) {
                if (portList[i] == this.portNumber) {
                    continue;
                }
                ThriftClient client = new ThriftClient(portList[i]);
                ret = client.sendStockRequest(request, ThriftClient.RequestStatus.SERVERSTOCKREQUEST);
                printConsole(ret);
            }
            if(request.getType().equals(Constant.STOCKS)) {
                ret = checkStocks(request);
            }
        }
        return ret;
    }

    @Override
    public String updateStockRequestFromServer(StockRequest request){
        String ret = null;
        if(request.getType().equals(Constant.STOCKS)) {
            ret = checkStocks(request);
        }
        return ret;
    }
}

