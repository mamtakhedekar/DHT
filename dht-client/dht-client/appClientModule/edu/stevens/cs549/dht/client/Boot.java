package edu.stevens.cs549.dht.client;

import java.util.logging.Logger;

import org.glassfish.appclient.client.acc.AppClientContainer;
import org.glassfish.appclient.client.acc.UserError;
import org.glassfish.appclient.client.acc.config.TargetServer;

public class Boot {

	private static Logger log = Logger.getLogger("edu.stevens.cs549.dht.client");

	public static void main(String[] args) {
		TargetServer[] servers = { new TargetServer() };

		// Get a builder to set up the ACC
		AppClientContainer.Builder builder = AppClientContainer.newBuilder(servers);

		// Fine-tune the ACC's configuration. Note ability to "chain" invocations.
		// builder.callbackHandler("com.acme.MyHandler").authRealm("myRealm");

		Class<Main> mainClass = Main.class;
		try {
			AppClientContainer acc = builder.newContainer(mainClass);

			// In either case, start the client running.
			String[] appArgs = {};
			acc.startClient(appArgs); // Start the client

			acc.stop(); // stop the ACC (optional)

		} catch (UserError e) {
			log.severe("User Error on starting up app client: "+e);
			e.printStackTrace();
		} catch (Exception e) {
			log.severe("Exception on starting up app client: "+e);
			e.printStackTrace();
		}
	}

}
