import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.util.HashMap;
import java.util.Map;

public class ThriftServer {
    public static ThriftHandler handler;

    public static ThriftService.Processor processor;
    private Map<Integer, Integer> mapping = new HashMap<>();

    public static void main(String [] args) {
        int portNumber =Integer.valueOf(args[0]);
        int[] portArr = {8080, 8081}; //8082, 8083, 8084

        try {
            handler = new ThriftHandler(portNumber, portArr);
            processor = new ThriftService.Processor(handler);

            Runnable simple = new Runnable() {
                public void run() {
                    threadPool(portNumber, processor);
                }
            };

            Runnable webSocketThread = new Runnable() {
                public void run() {
                    new MyWebSocketServer(retSocketPort(portNumber), handler).run();
                }
            };

            new Thread(webSocketThread).start();
            new Thread(simple).start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private static int retSocketPort(int number) {
        return number + 1000;
    }

    /**
     * This method is setup the thread pool
     * @param processor process of the MapService
     */
    public static void threadPool(int portNumber, ThriftService.Processor processor) {
        try {

            TServerTransport transport = new TServerSocket(portNumber);

            TThreadPoolServer.Args args = new TThreadPoolServer.Args(transport);
            args.minWorkerThreads(5);
            args.maxWorkerThreads(10);
            args.protocolFactory(new TBinaryProtocol.Factory());
            args.processor(processor);
            TThreadPoolServer server = new TThreadPoolServer(args);
            System.out.println("Starting the multithreading server in port: " + portNumber);
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}