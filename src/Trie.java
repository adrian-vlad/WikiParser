
public class Trie {
    private boolean is_word;
    private Trie l[];

    public Trie()
    {
        this.is_word = false;
        this.l = new Trie[26];
    }

    public boolean addWord(String s)
    {
        char w[] = new char[s.length()];
        s.getChars(0, w.length, w, 0);

        return this.addWord(w, 0, w.length);
    }

    public boolean addWord(char w[], int start, int len)
    {
        int pos = -1;
        boolean posAdded = false;
        if (0 == len)
        {
            return true;
        }

        if (false == isASCIIAlpha(w[start]))
        {
            return false;
        }

        pos = (w[start] | 32) - 97;
        if (null == this.l[pos])
        {
            this.l[pos] = new Trie();
            posAdded = true;
        }

        if (1 == len)
        {
            this.l[pos].is_word = true;
            return true;
        }

        boolean rc = this.l[pos].addWord(w, start + 1, len - 1);
        if (false == rc && posAdded)
        {
            this.l[pos] = null;
        }

        return rc;
    }

    public boolean searchWord(String s)
    {
        char w[] = new char[s.length()];
        s.getChars(0, w.length, w, 0);

        return this.searchWord(w, 0, w.length);
    }

    public boolean searchWord(char w[], int start, int len)
    {
        int pos = -1;

        if (0 == len)
        {
            return false;
        }

        if (false == isASCIIAlpha(w[start]))
        {
            return false;
        }

        pos = (w[start] | 32) - 97;
        if (null == this.l[pos])
        {
            return false;
        }

        if (1 == len)
        {
            return this.l[pos].is_word;
        }

        return this.l[pos].searchWord(w, start + 1, len - 1);
    }

    public static boolean isASCIIAlpha(int c)
    {
        if (('A' <= c && c <= 'Z') ||
            ('a' <= c && c <= 'z'))
        {
            return true;
        }

        return false;
    }

    public static boolean isAlphanumeric(int c) {
        if ((Character.isLetterOrDigit(c) == false) && (c != ' ')) {
            return false;
        }

        return true;
    }
}
