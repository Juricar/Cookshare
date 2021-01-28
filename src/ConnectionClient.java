/**
 * Makes a connection client
 * @author juricar
 *
 */
public class ConnectionClient {
	
	CookshareConnectionService chessdb;
	private final String dbName = "Cookshare";
	private final String serverName = "titan.csse.rose-hulman.edu";

	public static void main(String[] args) {
		new ConnectionClient();
	}
	
	public ConnectionClient() {
		chessdb = new CookshareConnectionService(serverName, dbName);
	}
}
