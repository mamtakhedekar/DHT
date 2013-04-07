/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stevens.cs549.dht.client;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;

import edu.stevens.cs549.dht.node.IDHTNode;
import edu.stevens.cs549.dht.rest.WebClient;
import edu.stevens.cs549.dht.routing.IRouting.Failed;
import edu.stevens.cs549.dht.state.Persist;

/*
 * CLI for a DHT node.
 */

public class CliClient {

	public static Logger log = Logger
			.getLogger("edu.stevens.cs549.dht.main.Client");
	

	protected IDHTNode node;
	
	protected Client client;
		
	protected long key;
	
	public CliClient(IDHTNode node) {
		this.node = node;
		this.client = Client.create();
		try {
			this.key = node.getNodeInfo().key;
		} catch (Failed e) {
			log.severe("Local node has already failed.");
		}
	}

	protected void msg(String m) {
		System.out.print(m);;
	}

	protected void msgln(String m) {
		System.out.println(m);
	}

	protected void err(Exception e) {
		log.severe("Error : " + e);
		e.printStackTrace();
	}
	
	public void cli() {

		// Main command-line interface loop

		Dispatch d = new Dispatch(client, node);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {
			while (true) {
				msg("dht<" + key + "> ");
				String line = in.readLine();
				String[] inputs = line.split("\\s+");
				if (inputs.length > 0) {
					String cmd = inputs[0];
					if (cmd.length() == 0)
						;
					else if ("get".equals(cmd))
						d.get(inputs);
					else if ("add".equals(cmd))
						d.add(inputs);
					else if ("delete".equals(cmd))
						d.delete(inputs);
					else if ("display".equals(cmd))
						d.display(inputs);
					else if ("insert".equals(cmd))
						d.insert(inputs);
					else if ("routes".equals(cmd))
						d.routes(inputs);
					else if ("fail".equals(cmd))
						d.fail(inputs);
					else if ("ping".equals(cmd))
						d.ping(inputs);
					else if ("silent".equals(cmd))
						d.silent(inputs);
					else if ("help".equals(cmd))
						d.help(inputs);
					else if ("quit".equals(cmd))
						return;
					else
						msgln("Bad input.  Type \"help\" for more information.");
				}
			}
		} catch (EOFException e) {
		} catch (IOException e) {
			err(e);
			System.exit(-1);
		}

	}

	protected class Dispatch {

		protected Client client;
		protected IDHTNode node;
		
		protected 

		Dispatch(Client c, IDHTNode n) {
			client = c;
			node = n;
		}

		public void help(String[] inputs) {
			if (inputs.length == 1) {
				msgln("Commands are:");
				msgln("  get key: get values under a key");
				msgln("  add key value: add a value under a key");
				msgln("  delete key value: delete a value under a key");
				msgln("  insert uri: insert as a new node into a DHT");
				msgln("  display: display all bindings");
				msgln("  routes: display routing information");
				msgln("  fail: mark this node as failed");
				msgln("  ping uri: check if a remote node is active");
				msgln("  silent: toggle on and off logging of background processing");
				msgln("  quit: exit the client");
			}
		}


		public void get(String[] inputs) {
			if (inputs.length == 2)
				try {
					String[] vs = node.get(inputs[1]);
					if (vs != null)
						msgln(Persist.displayVals(vs).toString());
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: get <key>");
		}

		public void add(String[] inputs) {
			if (inputs.length == 3)
				try {
					node.add(inputs[1], inputs[2]);
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: add <key> <value>");
		}

		public String stringArrToString(String[] arr )
		{
			String str = new String();
			str += "{";
			if (arr != null)
			{
				for ( int i = 0 ; i < arr.length -1; i++ )
				{
					str += arr[i];
					str += ",";
				}
				str += arr[arr.length -1];
			}
			str += "}";
			return str;
		}
		public void delete(String[] inputs) {
			if (inputs.length == 3)
				try {
					msgln(stringArrToString(node.delete(inputs[1], inputs[2])));
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: delete <key> <value>");
		}

		public void display(String[] inputs) {
			if (inputs.length == 1)
				try {
					msg(node.display().toString());
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: display");
		}

		public void routes(String[] inputs) {
			if (inputs.length == 1)
				try {
					msg(node.routes().toString());
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: routes");
		}

		public void fail(String[] inputs) {
			if (inputs.length == 1)
				try {
					node.setFailed();
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: routes");
		}

		public void silent(String[] inputs) {
			if (inputs.length == 1)
				try {
					msgln("Logging is always enabled in the app server.");
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: silent");
		}

		public void ping(String[] inputs) {
			if (inputs.length == 2)
				try {
					if (WebClient.isFailed(client, new URI(inputs[1]))) {
						msgln("Server down.");
					} else {
						msgln("Server up.");
					}
				} catch (URISyntaxException e) {
					msgln("Badly formed URI: "+inputs[1]);
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: ping <uri>");
		}

		public void insert(String[] inputs) {
			if (inputs.length == 2)
				try {
					node.insert(inputs[1]);
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: insert <uri>");
		}

	}

}
