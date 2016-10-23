package com.ags.aft.fixtures.ruby;

import org.apache.log4j.Logger;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;


public class EmbedRubyEngine {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(EmbedRubyEngine.class);
	private static ScriptingContainer container = null;

	/** The ScriptInstanceManager */
	private static EmbedRubyEngine rubyEngine;
	
	/** The ruby engine command */
	private static String rubyEnginecmd;
	
	/** The Ruby engine parent folder */
	private static String rubyEngineParent;
	
	
	
	public static EmbedRubyEngine getInstance() {
		if (rubyEngine == null) {
			rubyEngine = new EmbedRubyEngine();
			LOGGER.trace("Creating instance of ruby Engine");
		}

		return rubyEngine;
	}

	
	public void setRubyEngineEnv(String parentFolder) throws Exception
	{
		String[] values=parentFolder.split("\n");
		LOGGER.info("Setting ruby engine parent folder:"+values[0]);
		LOGGER.info("Setting ruby engine command:"+values[1]);
		rubyEngineParent=values[0];
		rubyEnginecmd=values[1];
	}
	
	public void startEngine() throws Exception
	{
		LOGGER.info("Embed ruby engine");
		if (container == null)
		{
			LOGGER.info("creating ruby engine container");
			// init Ruby instance
			try{
				container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
				container.setCompatVersion(org.jruby.CompatVersion.RUBY1_9);
				container.put("$log", LOGGER);
//				container.runScriptlet("Dir.chdir(\"D:/MTQA/SIVIEW\")");
//				container.runScriptlet("load \"./ruby/set_paths.rb\" ; set_paths");
//				container.runScriptlet("Dir.chdir(\"D:/Gf_Svn_Ruby_SRC/testconsole\")");
//				container.runScriptlet("load \"./ruby/mainExe.rb\";set_paths(env:\"mtqa\")");
				container.runScriptlet("Dir.chdir(\""+rubyEngineParent+"\")");
				container.runScriptlet(rubyEnginecmd);
				
			}catch(Exception e)
			{
				LOGGER.error("Issues with embedding ruby engine: " + e.getMessage());
				throw(e);
			}
		}
		else
		{
			LOGGER.info("Ruby engine is already embeded");
		}
	}
	
	public String runCommand(String command) throws Exception
	{
		try{
			return container.runScriptlet(command).toString();
		}catch(Exception e)
		{
			throw e;
		}
	}

	
	public static void main(String[] args){
		EmbedRubyEngine e=EmbedRubyEngine.getInstance();
		try {
//			e.setRubyEngineEnv("D:/Gf_Svn_Ruby_SRC/testconsole\nload \"./ruby/mainExe.rb\";set_paths(env:\"mtqa\")");
			
			e.setRubyEngineEnv("E:/Siview_RubyScripts/testconsole\nload \"./ruby/mainExe.rb\";set_paths(env:\"mtqa\")");
			e.startEngine();
	//		System.out.println(e.runCommand("console \"mtqa\""));
   //       String cmd="$mtqa.inhibit_exception_cancel('T500072.00',:eqp,'AUTO-EQP3')";
			String cmd="$mtqa8.lot_info("+"\"8XYM08032.000\")";
			System.out.println(e.runCommand(cmd));
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
	}
}
