package kaleidoscope.uploader.server;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class StartStopMain {
	
	private static AbstractApplicationContext applicationContext;
	
	private static final String DEFAULT_CONF = "META-INF/spring/spring-all-context.xml";
	
	public static void main(String[] args) throws Exception{
		
		String methodName = args[0];
		System.out.println("methodName=[" + methodName + "]");
		
		StringBuffer buff = new StringBuffer(); 
		for (int i = 0; i < args.length; i++) { 
			buff.append(args[i]).append(" ");
		}
		System.out.println("args=[" + buff + "]");
		
		if(methodName.equals("start"))
		{  		
			String conf = args[1];
			if(conf == null)
			{
				conf = DEFAULT_CONF;
			}
			
			start(conf);
		}
		else if(methodName.equals("stop"))
		{
			stop();
		}
		else
		{
			System.exit(1);
		}
		
	}
	
	public static void start(String conf) throws Exception
	{		
		System.out.println("call start()");
		
		applicationContext = new ClassPathXmlApplicationContext(
				new String[] { conf });
		applicationContext.registerShutdownHook();
	}  	

	public static void stop()
	{
		System.out.println("call stop()");
		
		applicationContext.close();
	}
}

