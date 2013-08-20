package kaleidoscope.uploader.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StartStopMain {
	private static Logger log = LoggerFactory.getLogger(StartStopMain.class);

	private static AbstractApplicationContext applicationContext;

	private static final String DEFAULT_CONF = "META-INF/spring/spring-all-context.xml";

	public static void main(String[] args) throws Exception {
		String method = args[0];

		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			buff.append(args[i]).append(" ");
		}

		log.info("args=[{}]", buff);

		if ("start".equalsIgnoreCase(method) == true) {
			String conf = args[1];
			if (conf == null) {
				conf = DEFAULT_CONF;
			}

			start(conf);
		} else if ("stop".equalsIgnoreCase(method) == true) {
			stop();
		} else {
			log.error("Usage (start|stop) [configure.xml]\n");
			System.exit(1);
		}
	}

	public static void start(String conf) throws Exception {
		System.out.println("call start()");

		applicationContext = new ClassPathXmlApplicationContext(
				new String[] { conf });
		applicationContext.registerShutdownHook();
	}

	public static void stop() {
		System.out.println("call stop()");

		applicationContext.close();
	}
}
