/**
 * Created with IntelliJ IDEA.
 * User: dominikfilipiak
 * Date: 17/10/13
 * Time: 23:17
 */
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

public class TFIDFSolver {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private static TFIDFSolver instance = null;

    Vector<String> database = new Vector<String>(); // the document collection
    TreeMap<String, Double> idfs = new TreeMap<String, Double>(); // idf value for each term in the vocabulary
    TreeMap<String, Set<Integer>> invertedFile = new TreeMap<String, Set<Integer>>(); // term -> docIds of docs containing the term
    Vector<TreeMap<String, Double>> tf = new Vector<TreeMap<String, Double>>(); // term x docId matrix with term frequencies
    ArrayList<String> keywords = null;


    public static TFIDFSolver getInstance(){
        if(instance == null){
            instance = new TFIDFSolver();
        }
        return instance;
    }

    public static void main(String [] args) {
        TFIDFSolver tfidf = new TFIDFSolver();
        tfidf.go("inf");
    }

    public void go(String query) {
        // init the database
//        initDatabase("/Volumes/Cornwall/Users/dominikfilipiak/Documents/Projects/tfidf-console/src/main/resources/db.txt");

        // init global variables: tf, invertedFile, and idfs
        init();

        // print the database
        printDatabase();

        // idfs and tfs
        System.out.println("IDFs:");
        // print the vocabulary
        printVocabulary();

        System.out.println("\nTFs for learn:");
        for (int i = 0; i < database.size(); i++)
        {
            System.out.println("sixteen: doc " + i + " : " + getTF("larger", i));
        }

        // similarities for different queries
        rank(query);
    }

    /**
     * inits database from text file
     * @param filename file with database
     */
    private void initDatabase(String filename) {
        database.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            while (reader.ready()) {
                String doc = reader.readLine().trim();
                database.add(doc);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No database available.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * lists the vocabulary
     */
    private void printVocabulary() {
        System.out.println("Vocabulary:");
        for (Map.Entry<String, Double> entry : idfs.entrySet()) {
            System.out.println(entry.getKey() + ", idf = " + entry.getValue());
        }
    }

    /**
     * lists the database
     */
    private void printDatabase() {
        System.out.println("size of the database: " + database.size());
        for (int i = 0; i < database.size(); i++) {
            System.out.println("doc " + i + ": " + database.elementAt(i));
        }
        System.out.println("");
    }

    // calculates the similarity between two vectors
    // each vector is a term -> weight map
    private double similarity(TreeMap<String, Double> v1, TreeMap<String, Double> v2) {
        double sum = 0;
        // iterate through one vector
        for (Map.Entry<String, Double> entry : v1.entrySet()) {
            String term = entry.getKey();
            Double w1 = entry.getValue();
            // multiply weights if contained in second vector
            Double w2 = v2.get(term);
            if (w2 != null)
                sum += w1 * w2;
        }
        // TODO write the formula for computation of cosinus [DONE]
        // note that v.values() is Collection<Double> that you may need to calculate length of the vector
        // take advantage of vecLength() function

        Double length1 = this.vecLength(v1.values());
        Double length2 = this.vecLength(v2.values());
        if(length1 == 0 || length2 == 0){
            return 0;
        } else {
            return sum/(length1*length2);//sum/(vecLength(v1.values())+ vecLength(v2.values()));
        }
    }

    // returns the length of a vector
    private double vecLength(Collection<Double> vec) {
        double sum = 0;
        for (Double d : vec) {
            sum += Math.pow(d, 2);
        }
        return Math.sqrt(sum);
    }

    // ranks a query to the documents of the database
    private void rank(String query) {
        System.out.println("");
        System.out.println("query = " + query);

        // get term frequencies for the query terms
        TreeMap<String, Double> termFreqs = getTF(query);

        // construct the query vector
        // the query vector
        TreeMap<String, Double> queryVec = new TreeMap<String, Double>();
        //TODO: tu gieremcio ma keywords
        // iterate through all query terms
        for (Map.Entry<String, Double> entry : termFreqs.entrySet()) {
            String term = entry.getKey();
            //TODO compute tfidf value for terms of query [DONE]
            if(!(keywords.contains(term))) continue;
            double tfidf = entry.getValue() * idf(term);
            queryVec.put(term, tfidf);
        }

        // helper class to store a docId and its score
        class DocScore implements Comparable<DocScore> {
            double score;
            int docId;

            public DocScore(double score, int docId) {
                this.score = score;
                this.docId = docId;
            }

            public int compareTo(DocScore docScore) {
                if (score > docScore.score) return -1;
                if (score < docScore.score) return 1;
                return 0;
            }
        }


        Set<Integer> union;
        TreeSet<String> queryTerms = new TreeSet<String>(termFreqs.keySet());

        // from the inverted file get the union of all docIDs that contain any query term

        union = new TreeSet<Integer>();//invertedFile.get(queryTerms.first());
        for (String term : queryTerms) {
            if(keywords.contains(term)) {
                union.addAll(invertedFile.get(term));
            }
        }

        // calculate the scores of documents in the union
        Vector<DocScore> scores = new Vector<DocScore>();
        for (Integer i : union) {
            scores.add(new DocScore(similarity(queryVec, getDocVec(i)), i));
        }

        // sort and print the scores
        Collections.sort(scores);
        for (DocScore docScore : scores) {
            System.out.println("score of doc " + docScore.docId + " = " + docScore.score);
        }
    }

    // returns the idf of a term
    private double idf(String term) {
        return idfs.get(term);
    }

    // calculates the document vector for a given docID
    private TreeMap<String, Double> getDocVec(int docId) {
        TreeMap<String, Double> vec = new TreeMap<String, Double>();

        // get all term frequencies
        TreeMap<String, Double> termFreqs = tf.elementAt(docId);

        // for each term, tf * idf
        for (Map.Entry<String, Double> entry : termFreqs.entrySet()) {
            String term = entry.getKey();
            //TODO compute tfidf value for a given term [DONE]
            //take advantage of idf() function
            double tfidf = entry.getValue() * this.idf(term);
            vec.put(term, tfidf);
        }
        return vec;
    }

    // returns the term frequency for a term and a docID
    private double getTF(String term, int docId) {
        Double freq = tf.elementAt(docId).get(term);
        if (freq == null) return 0;
        else return freq;
    }

    // calculates the term frequencies for a document
    private TreeMap<String, Double> getTF(String document) {
        TreeMap<String, Double> termFreqs = new TreeMap<String, Double>();
        double max = 0;

        // tokenize document
        StringTokenizer tokenizer = new StringTokenizer(document, " ");

        // for all tokens
        while (tokenizer.hasMoreTokens()) {
            //TODO: tutaj keywords?
            String term = tokenizer.nextToken();

            // count the max term frequency
            Double count = termFreqs.get(term);
            if (count == null) {
                count = new Double(0);
            }
            count++;
            termFreqs.put(term, count);
            if (count > max) {
                max = count;
            }
        }

        // normalize tf

        for (Map.Entry<String, Double> tf : termFreqs.entrySet()) {
            // TODO write the formula for normalization of TF [DONE]
            if(0 == max){
                tf.setValue(0.0);
            } else {
                tf.setValue(tf.getValue()/max);
            }

        }
        return termFreqs;
    }

    // init tf, invertedFile, and idfs
    private void init() {
        int documentId = 0;
        // for all docs in the database
        for (String document : database) {
            // get the tfs for a doc
            TreeMap<String, Double> termFrequency = getTF(document);

            // add to global tf vector
            tf.add(termFrequency);

            // for all terms
            for (String term : termFrequency.keySet()) {
                // add the current docID to the posting list
                Set<Integer> docIds = invertedFile.get(term);
                if (docIds == null) docIds = new TreeSet<Integer>();
                docIds.add(documentId);
                invertedFile.put(term, docIds);
            }
            documentId++;
        }

        // calculate idfs
        int dbSize = database.size();
        // for all terms
        for (Map.Entry<String, Set<Integer>> entry : invertedFile.entrySet()) {
            String term = entry.getKey();
            // get the size of the posting list, i.e. the document frequency
            int df = entry.getValue().size();
            //TODO write the formula for calculation of IDF [DONE]
            idfs.put(term, Math.log(dbSize/df));
        }
    }

    public Vector<String> getDatabase() {
        return database;
    }

    public void setDatabase(Vector<String> database) {
        this.database = database;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(ArrayList<String> keywords) {
        this.keywords = keywords;
    }
}

