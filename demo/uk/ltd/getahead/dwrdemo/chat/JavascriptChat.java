package uk.ltd.getahead.dwrdemo.chat;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.util.Logger;

/**
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class JavascriptChat
{
    /**
     * @param text The new message text to add
     */
    public void addMessage(String text)
    {
        if (text != null && text.trim().length() > 0)
        {
            messages.addFirst(new Message(text));
            while (messages.size() > 10)
            {
                messages.removeLast();
            }
        }

        WebContext wctx = WebContextFactory.get();
        String currentPage = wctx.getCurrentPage();

        ScriptBuffer script = new ScriptBuffer();
        script.appendScript("receiveMessages(")
              .appendData(messages)
              .appendScript(");");

        // Loop over all the users on the current page
        Collection pages = wctx.getScriptSessionsByPage(currentPage);
        for (Iterator it = pages.iterator(); it.hasNext();)
        {
            ScriptSession otherSession = (ScriptSession) it.next();
            otherSession.addScript(script);
        }
    }

    /**
     * The current set of messages
     */
    private LinkedList messages = new LinkedList();

    /**
     * 
     */
    public void pingMe()
    {
        WebContext wctx = WebContextFactory.get();
        final ScriptSession scriptSession = wctx.getScriptSession();
        Thread worker = new Thread(new Runnable()
        {
            public void run()
            {
                int count = 0;
                while (count < 100)
                {
                    count++;
                    try
                    {
                        Thread.sleep(1000);
                        log.debug("ping: " + count);
                        scriptSession.addScript(new ScriptBuffer("dwr.util.setValue('ping', 'count=" + count + "');"));
                    }
                    catch (Exception ex)
                    {
                        log.warn("Waking:", ex);
                    }
                }
            }
        });
        worker.start();
    }

    /**
     * The log stream
     */
    protected static final Logger log = Logger.getLogger(JavascriptChat.class);
}
