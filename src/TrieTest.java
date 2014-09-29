import junit.framework.TestCase;


public class TrieTest extends TestCase {

    public void testInvalidWord() {
        Trie t = new Trie();
        assertEquals(false, t.addWord("georgelĈ"));
        assertEquals(false, t.addWord("georĈgel"));
        assertEquals(false, t.addWord("Ĉgeorgel"));
        assertEquals(false, t.addWord("Ĉ"));
    }

    public void testGoodWord(){
        Trie t = new Trie();
        assertEquals(true, t.addWord("georgel"));
    }

    public void testOneSearch(){
        Trie t = new Trie();
        assertEquals(true, t.addWord("georgel"));
        assertEquals(true, t.searchWord("georgel"));
    }

    public void testMultipleSearch(){
        Trie t = new Trie();
        assertEquals(true, t.addWord("georgel"));
        assertEquals(true, t.searchWord("georgel"));
        assertEquals(true, t.searchWord("georgel"));
        assertEquals(true, t.searchWord("georgel"));
        assertEquals(true, t.searchWord("georgel"));
        assertEquals(false, t.searchWord("george"));
        assertEquals(false, t.searchWord("eorgel"));


        assertEquals(true, t.addWord("georgel"));
        assertEquals(true, t.addWord("george"));
        assertEquals(true, t.searchWord("george"));
        assertEquals(false, t.searchWord("georg"));
        assertEquals(false, t.searchWord("eorge"));
    }

}
