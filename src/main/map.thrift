namespace java project

struct UserRequest {
  1:string type;
  2:string username;
  3:string password;
  4:string identity;
}

struct ProductRequest{
    1:string type
    2:string itemName
    3:string stock
}

struct StockRequest{
    1:string type
}

 service ThriftService
 {
         string doCommit(),
         string updateRequestFromClient(1:UserRequest request),
         string updateRequestFromServer(1:UserRequest request),
         string updateProductRequestFromClient(1:ProductRequest request),
         string updateProductRequestFromServer(1:ProductRequest request),
         string updateStockRequestFromClient(1:StockRequest request),
         string updateStockRequestFromServer(1:StockRequest request),
 }