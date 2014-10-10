import java.io.File;

public class Main {

    private static boolean checkFile(String path)
    {
        boolean exists = false;
        File f = new File(path);

        exists = f.isFile() && f.canRead();

        if (false == exists)
        {
            System.err.println("Path '" + path + "' does not exist or it is not readable.");
        }

        return exists;
    }

    public static void main(String[] args) {
        Processor p = null;

        if (0 == args[0].compareTo("--help"))
        {
            System.out.println("Extracts the pages from a wikipedia dump xml that contains a specific word in title.\n"
                             + "This is a tool intended to be used for natural language processing and a helper for WikipediaESA tool.\n");
            System.out.println("WikiParser input_xml words_file output_xml\n");
            System.out.println("input_xml        path to the wikipedia dump xml or another xml that has the same structure\n"
                             + "                 from which the pages will be extracted");
            System.out.println("words_file       path to a file that contains the terms separated on each line used for\n"
                             + "                 selecting the pages. The words must contain only english letters.");
            System.out.println("output_xml       path to the file that will be created and will contain the extracted pages\n"
                             + "                 in a structure similar to the wikipedia dump xml");
            System.out.println("output_dir       path to a directory that will be created and will contain each page in a separate\n"
                             + "                 text file");
            System.exit(0);
        }

        if (4 > args.length)
        {
            System.err.println("Too few arguments. Check '--help' command for more information.");
            System.exit(1);
        }

        if (false == checkFile(args[0]) ||
            false == checkFile(args[1]))
        {
            System.err.println("Command line check failed. Check '--help' command for more information.");
            System.exit(1);
        }

        try {
            p = new Processor(args[0], args[1], args[2], args[3]);

            p.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
