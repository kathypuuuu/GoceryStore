import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThriftClient {
    /**
     * Port number this client wants to connect
     */
    private int portNumber;

    public enum RequestStatus {
        DOCOMMIT,
        CLIENTUSERREQUEST,
        SERVERUSERREQUEST,
        CLIENTPRODUCTREQUEST,
        SERVERPRODUCTREQUEST,
        CLIENTSTOCKREQUEST,
        SERVERSTOCKREQUEST,

    }

    public ThriftClient(int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * This method is used to send put/get/delete request to server
     */
    public String sendUserRequest(UserRequest request, RequestStatus status) {
        String ret = null;
        try {
            TTransport transport;

            TSocket socket = new TSocket("localhost", this.portNumber);
            socket.setConnectTimeout(5000);
            socket.setSocketTimeout(5000);
            transport = socket;
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            ThriftService.Client client = new ThriftService.Client(protocol);

            if (status == RequestStatus.DOCOMMIT) {
                ret = client.doCommit();
            } else if (status == RequestStatus.CLIENTUSERREQUEST) {
                ret = client.updateRequestFromClient(request);
            } else if(status == RequestStatus.SERVERUSERREQUEST) {
                ret = client.updateRequestFromServer(request);
            }
            transport.close();
        } catch(TTransportException x) {
            printConsole("Cannot connect to server with port number: " + this.portNumber);
        }catch (TException x) {
            x.printStackTrace();
        }

        return ret;
    }


    public String sendProductRequest(ProductRequest request, RequestStatus status) {
        String ret = null;
        try {
            TTransport transport;

            TSocket socket = new TSocket("localhost", this.portNumber);
            socket.setConnectTimeout(5000);
            socket.setSocketTimeout(5000);
            transport = socket;
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            ThriftService.Client client = new ThriftService.Client(protocol);

            if (status == RequestStatus.DOCOMMIT) {
                ret = client.doCommit();
            } else if (status == RequestStatus.CLIENTPRODUCTREQUEST) {
                ret = client.updateProductRequestFromClient(request);
            } else if(status == RequestStatus.SERVERPRODUCTREQUEST) {
                ret = client.updateProductRequestFromServer(request);
            }
            transport.close();
        } catch(TTransportException x) {
            printConsole("Cannot connect to server with port number: " + this.portNumber);
        }catch (TException x) {
            x.printStackTrace();
        }

        return ret;
    }

    public String sendStockRequest(StockRequest request, RequestStatus status) {
        String ret = null;
        try {
            TTransport transport;

            TSocket socket = new TSocket("localhost", this.portNumber);
            socket.setConnectTimeout(5000);
            socket.setSocketTimeout(5000);
            transport = socket;
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            ThriftService.Client client = new ThriftService.Client(protocol);

            if (status == RequestStatus.DOCOMMIT) {
                ret = client.doCommit();
            } else if (status == RequestStatus.CLIENTSTOCKREQUEST) {
                ret = client.updateStockRequestFromClient(request);
            } else if(status == RequestStatus.SERVERSTOCKREQUEST) {
                ret = client.updateStockRequestFromServer(request);
            }
            transport.close();
        } catch(TTransportException x) {
            printConsole("Cannot connect to server with port number: " + this.portNumber);
        }catch (TException x) {
            x.printStackTrace();
        }

        return ret;
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
}