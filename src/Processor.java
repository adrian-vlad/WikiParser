import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.text.Normalizer.Form;

public class Processor {
    private BufferedWriter outXmlFd;
    private BufferedWriter outFileFd;
    private Parser p;
    private Trie t;

    private boolean inTitle;
    private String currentTitle;
    private boolean foundTitle;
    private boolean inText;
    private String outDir;
    private boolean firstLineOfText;

    public Processor(String inFile, String wordsFile, String outFileXml, String outDir) throws Exception
    {
        this.p = new Parser(inFile);
        this.t = new Trie();

        this.currentTitle = "";
        this.inTitle = false;
        this.foundTitle = false;
        this.inText = false;
        this.outDir = outDir;
        this.firstLineOfText = true;

        if (null != outFileXml && 0 < outFileXml.length())
        {
            this.outXmlFd = openFileForWrite(outFileXml);
        }

        if (null != outDir && 0 < outDir.length())
        {
            if (!makeDir(outDir))
            {
                throw new Exception("Error creating directory: " + outDir);
            }
        }

        this.loadTrie(wordsFile);
    }

    private static boolean makeDir(String dirPath)
    {
        boolean ret = true;
        File directory = new File(dirPath);

        if (directory.exists() && directory.isFile())
        {
            System.err.println("The dir with name could not be created as it is a normal file");

            return false;
        }

        if (!directory.exists())
        {
            ret = directory.mkdir();
        }

        if (ret)
        {
//            ret = purgeDirectory(directory);
        }

        return ret;
    }

    private static boolean purgeDirectory(File dir)
    {
        boolean ret = true;

        for (File file: dir.listFiles())
        {
            if (file.isDirectory())
            {
                ret = purgeDirectory(file);
            }

            if (ret)
            {
                ret = file.delete();
                if (!ret)
                {
                    System.err.println("Failed to remove file/folder: " + file.getAbsolutePath());
                }
            }

            if (!ret)
            {
                break;
            }
        }

        return ret;
    }

    private BufferedWriter openFileForWrite(String path)
    {
        BufferedWriter fd = null;

        try {
            fd = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fd;
    }

    private void loadTrie(String f)
    {
        BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            while ((line = br.readLine()) != null) {
                this.t.addWord(line);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isTitleGood()
    {
        boolean titleGood = false;
        int start = -1;

        for (int i = 0; i < this.currentTitle.length(); i++)
        {
            if (!Trie.isASCIIAlpha(this.currentTitle.charAt(i)))
            {
                if (-1 != start)
                {
                    String word = this.currentTitle.substring(start, i);

                    titleGood = this.t.searchWord(word);
                    if (titleGood)
                    {
                        return true;
                    }

                    start = -1;
                }
            }
            else
            {
                start = (-1 == start) ? i : start;
            }
        }

        if (-1 != start)
        {
            return this.t.searchWord(this.currentTitle.substring(start, this.currentTitle.length()));
        }

        return false;
    }

    private static String createFileNameFromTitle(String title)
    {
        String normalized = Normalizer.normalize(title, Form.NFD);

        return normalized.replaceAll("[^A-Za-z0-9]", "");
    }

    private void startTag()
    {
        String tag = this.p.getLastTag();

        this.firstLineOfText = true;

        if (0 == "page".compareTo(tag))
        {
        }
        if (0 == "title".compareTo(tag))
        {
            this.currentTitle = "";
            this.inTitle = true;
        }
        if (0 == "text".compareTo(tag))
        {
            this.inText = true;

            if (this.foundTitle)
            {
                try {
                    this.outXmlFd.write("\n    <text>\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopTag()
    {
        String tag = this.p.getLastTag();

        if (0 == "page".compareTo(tag))
        {
            if (true == this.foundTitle)
            {
                try {
                    this.outXmlFd.write("\n  </page>");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            this.foundTitle = false;
        }
        if (0 == "title".compareTo(tag))
        {
            this.inTitle = false;

            if (this.isTitleGood())
            {
                this.foundTitle = true;

                try {
                    this.outXmlFd.write("\n  <page>\n    <title>" + currentTitle + "</title>");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.outFileFd = openFileForWrite(this.outDir + File.pathSeparator + createFileNameFromTitle(this.currentTitle));
            }
        }
        if (0 == "text".compareTo(tag))
        {
            this.inText = false;

            if (true == this.foundTitle)
            {
                try {
                    this.outXmlFd.write("\n    </text>");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    this.outFileFd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void chars()
    {
        if (true == this.inTitle)
        {
            this.currentTitle += (char)this.p.getLastChar();
        }
        if (true == this.foundTitle && true == this.inText)
        {
            try {
                /* write the text to the xml file */
                this.outXmlFd.write(p.getLastChar());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                /* write the title of the page */
                if (this.firstLineOfText)
                {
                    this.outFileFd.write(createFileNameFromTitle(this.currentTitle));
                    this.outFileFd.write("\n");
                }

                /* write the text to the individual file */
                this.outFileFd.write(p.getLastChar());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.firstLineOfText = false;
    }

    public void process()
    {
        Parser.Events event = Parser.Events.ev_TAG;

        try {
            outXmlFd.write("<mediawiki>");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (Parser.Events.ev_NONE != event)
        {
            event = this.p.parse();

            switch(event)
            {
                case ev_NONE:
                    break;
                case ev_TAG:
                    this.startTag();
                    break;
                case ev_TEXT_CHAR:
                    this.chars();
                    break;
                case ev_END_TAG:
                    this.stopTag();
                    break;
                case ev_TAG_NO_END:
                    break;
            }
        }

        try {
            this.outXmlFd.write("</mediawiki>");

            outXmlFd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
