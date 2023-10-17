package ch.ehi.gbdbsvalidator.gui;


/** Listener that logs errors including validation results to the GUI.
 */
public class LogListener {
	MainFrame out=null;
	public LogListener(MainFrame out1) {
		out=out1;
	}

	public void outputMsgLine(String msg) {
		if(msg.endsWith("\n")){
			out.logAppend(msg);
		}else{
			out.logAppend(msg+"\n");
		}
	}

}
