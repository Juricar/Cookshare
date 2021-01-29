/**
 * Makes a connection client
 * @author juricar
 *
 */
public class ConnectionClient {
	
	CookshareConnectionService db;
	private final String dbName = "Cookshare";
	private final String serverName = "titan.csse.rose-hulman.edu";

	public static void main(String[] args) {
		new ConnectionClient();
	}
	
	public ConnectionClient() {
		db = new CookshareConnectionService(serverName, dbName);
	}
}
