import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Stack;


public class Parser {

    private final int BUFFER_SIZE = 1024 * 1024;
    private enum States{
        s_del,          // delimiters: spaces, maybe tabs
        s_small,        // "<"
        s_tag,          // the tag, <"this_thing_here">
        s_tag_gt,       // ">"
        s_tag_delim,    // "\r\n", "\n"
        s_text,         // the text for that tag
        s_end_tag_sm,   // "<" for the tag ending
        s_end_tag_slash,// "/"
        s_end_tag,      // the end tag, </"this_thing_here">
        s_end_tag_gt,   // ">" for the tag ending
        s_doctype,      // "!"
        s_doctype_gt,   // ">"
        s_tag_space,    // space before an attribute: <tag att=value>
        s_tag_att,      // the tag's attributes
        s_tag_slash,    // <tag att1=1 att2=2 ... />
        s_tag_no_end_gt,// identified a tag that hasn't a closing tag
        s_error,        // an error state
    }

    public enum Events{
        ev_NONE,
        ev_TAG,
        ev_TEXT_CHAR,
        ev_END_TAG,
        ev_TAG_NO_END,
    }

    BufferedReader in;
    States state;
    Events event;
    boolean event_changed;
    String current_tag;
    int current_char;
    Stack<String> tags;
    int charsRead;

    public Parser(String inFile)
    {
        try {
            this.in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF-8"), BUFFER_SIZE);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.state = Parser.States.s_del;
        this.event = Parser.Events.ev_NONE;
        this.event_changed = false;
        this.current_tag = "";
        this.current_char = 0;
        this.tags = new Stack<String>();
        this.charsRead = 0;
    }

    public Parser.Events parse()
    {
        int c = 0;
        boolean ret = true;

        try {
            while ((c = in.read()) != -1 && ret)
            {
                this.charsRead += 1;

                ret = this.updateState(c);
                if (ret)
                {
                    updateEvent();
                    ret = updateStrings(c);
                }

                if (ret && this.event_changed)
                {
                    return this.event;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!ret)
        {
            System.err.println("Error at position [" + this.charsRead + "] while parsing the input file.");
            while (this.tags.size() != 0)
            {
                System.out.println(this.tags.pop());
            }
            System.out.println(this.current_tag);
        }

        return Parser.Events.ev_NONE;
    }

    private boolean updateState(int i)
    {
        int c = (char)i;
        switch(this.state)
        {
            case s_del:
                this.state = ('<' == c) ? Parser.States.s_small :
                             (('\n' == c || ' ' == c) ? Parser.States.s_del : Parser.States.s_error);
                break;
            case s_small:
                this.state = ('/' == c) ? Parser.States.s_end_tag_slash :
                             (('!' != c) ? Parser.States.s_tag : Parser.States.s_doctype);
                break;
            case s_tag:
                this.state = ('>' == c) ? Parser.States.s_tag_gt :
                             ((' ' == c) ? Parser.States.s_tag_space :
                             (('/' == c) ? Parser.States.s_tag_slash : Parser.States.s_tag));
                break;
            case s_tag_gt:
                this.state = ('<' == c) ? Parser.States.s_small :
                             (('\n' == c || ' ' == c) ? Parser.States.s_tag_delim : Parser.States.s_text);
                break;
            case s_tag_delim:
                this.state = ('\n' == c || ' ' == c) ? Parser.States.s_tag_delim :
                             (('<' == c) ? Parser.States.s_small : Parser.States.s_text);
                break;
            case s_text:
                this.state = ('<' == c) ? Parser.States.s_end_tag_sm : Parser.States.s_text;
                break;
            case s_end_tag_sm:
                this.state = ('/' == c) ? Parser.States.s_end_tag_slash : Parser.States.s_error;
                break;
            case s_end_tag_slash:
                this.state = Parser.States.s_end_tag;
                break;
            case s_end_tag:
                this.state = ('>' == c) ? Parser.States.s_end_tag_gt : Parser.States.s_end_tag;
                break;
            case s_end_tag_gt:
                this.state = ('<' == c) ? Parser.States.s_small :
                             (('\n' == c || ' ' == c) ? Parser.States.s_del : Parser.States.s_error);
                break;
            case s_doctype:
                this.state = ('>' == c) ? Parser.States.s_doctype_gt : Parser.States.s_doctype;
                break;
            case s_doctype_gt:
                this.state = ('<' == c) ? Parser.States.s_small :
                             (('\n' == c || ' ' == c) ? Parser.States.s_del : Parser.States.s_error);
                break;
            case s_tag_space:
                this.state = (' ' == c) ? Parser.States.s_tag_space :
                             (('/' == c) ? Parser.States.s_tag_slash : Parser.States.s_tag_att);
                break;
            case s_tag_att:
                this.state = ('/' == c) ? Parser.States.s_tag_slash :
                             (('>' == c) ? Parser.States.s_tag_gt : Parser.States.s_tag_att);
                break;
            case s_tag_slash:
                this.state = ('>' == c) ? Parser.States.s_tag_no_end_gt : Parser.States.s_tag_att;
                break;
            case s_tag_no_end_gt:
                this.state = ('<' == c) ? Parser.States.s_small :
                             (('\n' == c || ' ' == c) ? Parser.States.s_del : Parser.States.s_error);
                break;
            default:
                System.err.println("Unknown state" + this.state);
                return false;
        }

        if (Parser.States.s_error != this.state)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void updateEvent()
    {
        this.event_changed = false;
        switch(this.state)
        {
            case s_del:
                break;
            case s_small:
                break;
            case s_tag:
                break;
            case s_tag_gt:
                this.event = Parser.Events.ev_TAG;
                this.event_changed = true;
                break;
            case s_tag_delim:
                break;
            case s_text:
                this.event = Parser.Events.ev_TEXT_CHAR;
                this.event_changed = true;
                break;
            case s_end_tag_sm:
                break;
            case s_end_tag_slash:
                break;
            case s_end_tag:
                break;
            case s_end_tag_gt:
                this.event = Parser.Events.ev_END_TAG;
                this.event_changed = true;
                break;
            case s_doctype:
                break;
            case s_doctype_gt:
                break;
            case s_tag_space:
                break;
            case s_tag_att:
                break;
            case s_tag_slash:
                break;
            case s_tag_no_end_gt:
                this.event = Parser.Events.ev_TAG_NO_END;
                this.event_changed = true;
                break;
            default:
                System.err.println("Unknown state" + this.state);
        }
    }

    private boolean updateStrings(int i)
    {
        int c = (char) i;
        switch(this.state)
        {
            case s_del:
                break;
            case s_small:
                this.current_tag = "";
                break;
            case s_tag:
                this.current_tag += (char)c;
                break;
            case s_tag_gt:
                this.tags.push(this.current_tag);
                break;
            case s_tag_delim:
                break;
            case s_text:
                this.current_char = c;
                break;
            case s_end_tag_sm:
                this.current_tag = "";
                break;
            case s_end_tag_slash:
                break;
            case s_end_tag:
                this.current_tag += (char)c;
                break;
            case s_end_tag_gt:
                if (0 >= this.tags.size())
                {
                    System.err.println("Ending tag error!");
                    return false;
                }
                String eTag = this.tags.pop();
                if (0 != this.current_tag.compareTo(eTag))
                {
                    System.err.println("Ending tag error!");
                    return false;
                }
                break;
            case s_doctype:
                break;
            case s_doctype_gt:
                break;
            case s_tag_space:
                break;
            case s_tag_att:
                break;
            case s_tag_slash:
                break;
            case s_tag_no_end_gt:
                this.current_tag += (char)c;
                break;
            default:
                System.err.println("Unknown state" + this.state);
                return false;
        }

        return true;
    }

    public String getLastTag()
    {
        return this.current_tag;
    }
    public int getLastChar()
    {
        return this.current_char;
    }
}
