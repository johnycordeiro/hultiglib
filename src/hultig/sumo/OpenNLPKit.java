/*************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2011 UBI/HULTIG All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 3 only ("GPL"). You may not use this
 * file except in compliance with the License. You can obtain a copy of
 * the License at http://www.gnu.org/licenses/gpl.txt. See the License
 * for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the software, include this License Header Notice
 * in each file. If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 *
 *       "Portions Copyrighted [year] [name of copyright owner]"
 *************************************************************************
 */
package hultig.sumo;

import hultig.io.HULTIGVars;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.chunker.ChunkerME;

import hultig.io.Input;
import hultig.util.OSArguments;
import hultig.util.Toolkit;
import java.util.HashMap;
import java.util.List;


/**
 * <p>
 * This class gathers and simplifies the access
 * to the main features of the OpenNLP package, as part-of-speech tagging and
 * sentence parsing. The package must already have been installed and its
 * installation path must be supplied to the constructor of this class, as
 * exemplified bellow:
 * </p>
 * <pre>
 *  OpenNLPKit model = new OpenNLPKit("/a/tools/opennlp-tools-1.5.0/models/english/");
 * </pre>
 * <p>
 * The {@link #main(java.lang.String[]) main} method performs a general test,
 * demonstrating the class key features.
 * </p>
 * 
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @author João Paulo Cordeiro (2007)
 * @version 1.0
 */
public final class OpenNLPKit
{
    private String modelsPATH;

    /**
     * The language model for sentence boundary detection.
     */
    private SentenceDetectorME stdetec= null;

    /**
     * The language model for string tokenization.
     */
    private Tokenizer tokenizer= null;

    /**
     * The language model for part-of-speech tagging.
     */
    private POSTaggerME tagger= null;

    /**
     * The language model for sentence chunking, that
     * is shallow parsing.
     */
    private ChunkerME chunker= null;

    /**
     * The language model for parsing a sentence (full parsing).
     */
    private Parser parser= null;

    /**
     * The sentence detector reference code.
     */
    public static final int STDETECT=  0;

    /**
     * The tokenizer reference code.
     */
    public static final int TOKENIZER= 1;

    /**
     * The tagger reference code.
     */
    public static final int TAGGER=    2;

    /**
     * The chunker reference code.
     */
    public static final int CHUNKER=   3;

    /**
     * The parser reference code.
     */
    public static final int PARSER=    4;

    /**
     * An array containing the file names of the language models,
     * from the sentence detector until the parser.
     */
    public static String[] modelFileName;


    private boolean SILENTMODE= false;

    /**
     * The default constructor initializes the models path with null. This
     * means that this path must be defined later, in order to be able to
     * use this object. Each model filename is also defined here, in the
     * {@link #modelFileName modelFileName} array.
     */
    public OpenNLPKit() {
        modelsPATH= null;
        modelFileName= new String[5];
        modelFileName[STDETECT]=  "en-sent.bin";
        modelFileName[TOKENIZER]= "en-token.bin";
        modelFileName[TAGGER]=    "en-pos-maxent.bin";
        modelFileName[CHUNKER]=   "en-chunker.bin";
        modelFileName[PARSER]=    "en-parser-chunking.bin";
    }

    /**
     * Creates an OpenNLP kit trying to define the path for the main directory
     * containing the language models. If the path is not a valid OS directory
     * then the {@link #modelsPATH modelsPATH} variable is set to null.
     * @param path The string path
     */
    public OpenNLPKit(String path) {
        this();
        if ( !setModelsPath(path) )
            modelsPATH= null;
    }

    /**
     * Sets and validates a given path as being a valid directory.
     * In order to subsequently find the language models, the
     * path must point to the directory containing these modules.
     * @param path The string path
     * @return The true value if the path points to a valid OS
     * directory.
     */
    public boolean setModelsPath(String path) {
        File fpath= new File(path);
        if ( !fpath.isDirectory() ) return false;
        modelsPATH= path;
        return true;
    }

    /**
     * Sets the "silent mode" state, used in some methods for printing
     * log/info/status messages.
     * @param value True for activation and false for deactivation.
     */
    public void setSilentMode(boolean value) {
        SILENTMODE= value;
    }

    /**
     * Tries to load all the language models from the defined
     * path {@link #modelsPATH modelsPATH}.
     * @return True means just that the the path is well defined,
     * targeting the assumed modules main directory.
     */
    public boolean loadAllModels() {
        if ( modelsPATH == null )  return false;
        if ( !SILENTMODE ) System.out.println("LOADING OPEN NLP LANGUAGE MODELS ...");
        loadSentenceDetector();
        loadTokenizer();
        loadTagger();
        loadChunker();
        loadParser();
        if ( !SILENTMODE ) System.out.println("OPEN NLP LANGUAGE MODELS LOADED");
        return true;
    }

    /**
     * Tries to load the model necessary for sentence detection. On error the model
     * will not be loaded, meaning that the {@link #stdetec stdetect} variable
     * still be equal to <b>null</b> or to the old model object.
     */
    public void loadSentenceDetector() {
        InputStream modelIn= null;
        try {
            if ( !SILENTMODE ) System.out.print("[X] - LOADING SENTENCE DETECTOR MODEL ...");
            if ( modelsPATH == null ) {
                ClassLoader cl = this.getClass().getClassLoader();
                modelIn = cl.getResourceAsStream("models/english/"+modelFileName[STDETECT]);
            }
            else {
                modelIn = new FileInputStream(modelsPATH+modelFileName[STDETECT]);
            }
            SentenceModel model = new SentenceModel(modelIn);
            if ( !SILENTMODE ) System.out.println(" SENTENCE DETECTOR MODEL LOADED");
            stdetec= new SentenceDetectorME(model);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Tries to load the model necessary for string tokenization. On error the model
     * will not be loaded, meaning that the {@link #tokenizer tokenizer} variable
     * still be equal to <b>null</b> or to the old model object.
     */
    public void loadTokenizer() {
        if ( stdetec == null )  this.loadSentenceDetector();
        InputStream modelIn= null;
        try {
            if ( !SILENTMODE ) System.out.print("[X] - LOADING TOKENIZER MODEL ...........");
            if ( modelsPATH == null ) {
                ClassLoader cl = this.getClass().getClassLoader();
                modelIn = cl.getResourceAsStream("models/english/"+modelFileName[TOKENIZER]);
            }
            else {
                modelIn = new FileInputStream(modelsPATH+modelFileName[TOKENIZER]);
            }
            TokenizerModel model = new TokenizerModel(modelIn);
            if ( !SILENTMODE ) System.out.println(" TOKENIZER MODEL LOADED");
            tokenizer= new TokenizerME(model);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Tries to load the model necessary for part-of-speech tagging. On error the model
     * will not be loaded, meaning that the {@link #tagger tagger} variable
     * still be equal to <b>null</b> or to the old model object.
     */
    public void loadTagger() {
        if ( tokenizer == null )  this.loadTokenizer();
        InputStream modelIn = null;
        try {
            if ( !SILENTMODE ) System.out.print("[X] - LOADING TAGGER MODEL ..............");
            if ( modelsPATH == null ) {
                ClassLoader cl = this.getClass().getClassLoader();
                modelIn = cl.getResourceAsStream("models/english/"+modelFileName[TAGGER]);
            }
            else {
                modelIn = new FileInputStream(modelsPATH+modelFileName[TAGGER]);
            }
            POSModel model = new POSModel(modelIn);
            if ( !SILENTMODE ) System.out.println(" TAGGER MODEL LOADED");
            tagger= new POSTaggerME(model);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Tries to load the model necessary for sentence chunking, that is shallow
     * parsing. On error the model will not be loaded, meaning that the
     * {@link #chunker chunker} variable still be equal to <b>null</b> or to
     * the old model object.
     */
    public void loadChunker() {
        if ( tagger == null )  this.loadTagger();
        InputStream modelIn = null;
        try {
            if ( !SILENTMODE ) System.out.print("[X] - LOADING CHUNKER MODEL .............");
            if ( modelsPATH == null ) {
                ClassLoader cl = this.getClass().getClassLoader();
                modelIn = cl.getResourceAsStream("models/english/"+modelFileName[CHUNKER]);
            }
            else {
                modelIn = new FileInputStream(modelsPATH+modelFileName[CHUNKER]);
            }
            ChunkerModel model = new ChunkerModel(modelIn);
            if ( !SILENTMODE ) System.out.println(" CHUNKER MODEL LOADED");
            chunker= new ChunkerME(model);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Tries to load the model necessary for sentence parsing (full parsing).
     * On error the model will not be loaded, meaning that the
     * {@link #parser parser} variable still be equal to <b>null</b> or to
     * the old model object.
     */
    public void loadParser() {
        if ( tagger == null )  this.loadTagger();
        InputStream modelIn = null;
        try {
            if ( !SILENTMODE ) System.out.print("[X] - LOADING PARSER MODEL ..............");
            if ( modelsPATH == null ) {
                ClassLoader cl = this.getClass().getClassLoader();
                modelIn = cl.getResourceAsStream("models/english/"+modelFileName[PARSER]);
            }
            else {
                modelIn = new FileInputStream(modelsPATH+modelFileName[PARSER]);
            }
            ParserModel model = new ParserModel(modelIn);
            parser= ParserFactory.create(model);
            if ( !SILENTMODE ) System.out.println(" PARSER MODEL LOADED");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Tests if the sentence detection model is well defined.
     * @return The <b>true</b>/<b>false</b> values uppon
     * success/failure.
     */
    public boolean definedSentenceDetector() {
        if ( stdetec == null )
            return false;
        else
            return true;
    }

    /**
     * Tests if all the language models are well loaded and defined.
     * This method uses {@link #allDefined(boolean) allDefined(boolean)} to
     * Silently make the verification, meaning that no output message will
     * be printed.
     * @return Gives <b>true</b> only if all language models are well
     * defined.
     */
    public boolean allDefined() {
        return allDefined(true);
    }
    
    /**
     * Tests is all language models are well loaded and defined.
     * @param silent If activated (true) avoids printing any
     * error message.
     * @return Gives <b>true</b> only if all language models are
     * well defined.
     */
    public boolean allDefined(boolean silent) {
        if ( stdetec == null ) {
            if ( !silent )  System.err.print("   Sentence detector failed!\n");
            return false;
        }
        if ( tokenizer == null ) {
            if ( !silent )  System.err.print("   Tokenizer failed!\n");
            return false;
        }
        if ( tagger == null ) {
            if ( !silent )  System.err.print("   Tagger failed!\n");
            return false;
        }
        if ( chunker == null ) {
            if ( !silent )  System.err.print("   Chunker failed!\n");
            return false;
        }

        if ( parser == null ) {
            if ( !silent )  System.err.print("   Parser failed!\n");
            return false;
        }
        
        return true;
    }

    /**
     * Verifies if a specific model is loaded. If not tries to load
     * it. If the loading process fails, the configuration for that
     * model is intrepreted as not being ok.
     * @param modelcode The model code.
     * @return The <b>true</b> value if a given model is active (loaded).
     */
    private boolean configurationOK(int modelcode) {
        switch ( modelcode ) {
            case STDETECT:
                if ( stdetec == null ) {
                    loadSentenceDetector();
                    if ( stdetec == null ) return false;
                }
                break;
            case TOKENIZER:
                if ( tokenizer == null ) {
                    this.loadTokenizer();
                    if ( tokenizer == null ) return false;
                }
                break;
            case TAGGER:
                if ( tagger == null ) {
                    this.loadTagger();
                    if ( tagger == null ) return false;
                }
                break;
            case CHUNKER:
                if ( chunker == null ) {
                    this.loadChunker();
                    if ( chunker == null ) return false;
                }
                break;
            case PARSER:
                if ( parser == null ) {
                    this.loadParser();
                    if ( parser == null ) return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Splits an assumed textual string, having possibly several sentences,
     * into an array of strings, with one sentence per position.
     * @param stxt The textual string.
     * @return The array of sentences or null.
     */
    public String[] splitSentences(String stxt) {
        if ( stxt == null || !configurationOK(STDETECT) )
            return null;
        else
            return stdetec.sentDetect(stxt);
    }

    /**
     * Process the Part-of-Speech tagging for a given textual string.
     * @param stxt The textual string.
     * @return The tagged string.
     */
    public String postag(String stxt) {
        if ( stxt == null || !configurationOK(TAGGER) )  return null;
        
        String[] tokens = tokenizer.tokenize(stxt);
        StringBuffer tline = (tokens.length > 0) ?  new StringBuffer(tokens[0]) : new StringBuffer("");
        for (int ti = 1, tn = tokens.length; ti < tn; ti++) {
            tline.append(' ').append(tokens[ti]);
        }
        return tagger.tag(tline.toString());
    }

    /**
     * Process the Part-of-Speech tagging for a given
     * Sentence. This method will reconstruct the
     * Sentence object, returning a new tagged
     * one, where every word is POS tagged.
     * @param s Sentence The sentence to be tagged.
     * @return Sentence The tagged sentence.
     */
    public Sentence postag(Sentence s) {
        if ( s == null || !configurationOK(TAGGER) )  return null;
        /*
        HashMap<String, Integer> wordKeys= new HashMap<>();
        for (Word w : s) {
            int lxc= w.getLexCod();
            if ( lxc > -1 )
                wordKeys.put(w.toString(), lxc);
        }
        */
        String[] vs= postag(s.toString()).split("\\s+");
        //List<String> vs= Sentence.tokenize(postag(s.toString()));
        Sentence sx= new Sentence();

        for (String tk : vs) {
            String token, potag;
            int r= tk.lastIndexOf('/');
            if ( r == -1 ) {
                token= tk;
                potag= "UNK";
                //s.addWord(new Word(vs[k], "UNK"));
            }
            else {
                token= tk.substring(0,r);
                potag= tk.substring(r+1);
            }
            Word w = new Word(token, potag);
            //Integer lxc = wordKeys.get(token);
            //if (lxc != null ) w.setLexCod(lxc);
            sx.addWord(w);
        }

        return sx;
    }
    
    
    
    public Text postag(Text ti) {
        if ( ti == null || !configurationOK(TAGGER) )  return null;
        Text to= new Text();
        for (Sentence s : ti) {
            to.add(postag(s));
        }
        return to;
    }
    

    /**
     * This method shallow parses a given sentence string. It obtained and
     * adapted from the file opennlp-tools.models.english.chunker.TreebankChunker.java.
     * @param tagedline The tagged sentence string to be chunked.
     * @return String The shallow parsed sentence.
     */
    public String chunk(String tagedline) {
        if ( chunker == null )  return null;
        
        String[] tts = tagedline.split("  *");
        String[] tokens = new String[tts.length];
        String[] tags = new String[tts.length];
        for (int ti = 0, tn = tts.length; ti < tn; ti++) {
            try {
                int r= tts[ti].lastIndexOf('/');
                tokens[ti] = tts[ti].substring(0, r);
                tags[ti] = tts[ti].substring(r+1);
            }
            catch (Exception exc) {
                System.out.printf("---> [%s] <---\n", tts[ti]);
                exc.printStackTrace();
            }
        }
        String[] chunks = chunker.chunk(tokens, tags);

        String finalpunct= ".?!";
        StringBuilder outline = new StringBuilder("");
        for (int i = 0, n = chunks.length; i < n; i++) {

            if (i > 0 && !chunks[i].startsWith("I-") && !chunks[i - 1].equals("O")) {
                outline.append(" ]");
            }
            if (chunks[i].startsWith("B-")) {
                outline.append(" [").append(chunks[i].substring(2));
            }

            outline.append("  ").append(tokens[i]).append("/").append(tags[i]);
            if ( finalpunct.indexOf(tags[i]) >= 0  &&  i<n-1  &&  finalpunct.indexOf(tags[i+1]) < 0 )
                outline.append("\n\n");
        }
        if (!chunks[chunks.length - 1].equals("O")) {
            outline.append(" ]");
        }

        return outline.toString();
    }

    /**
     * Gives the shallow parsed string from an already marked sentence.
     * @param stc The marked sentence.
     * @return String The shallow parsed string.
     */
    public String chunk(Sentence stc) {
        if ( tagger == null || chunker == null )  return null;
        
        if ( stc == null )  return null;
        if ( stc.get(0).getTag().length() == 0 )  stc= postag(stc);

        return chunk(stc.toStringPOS());
    }

    /**
     * Fully parses a sentence contained in a string.
     * @param stc The sentence string.
     * @return The parsed string.
     */
    public String parse(String stc) {
        if ( stc == null || parser == null ) return null;

        Parse topParses[] = ParserTool.parseLine(stc, parser, 1);
        StringBuffer sb= new StringBuffer();
        topParses[0].show(sb);
        return sb.toString();
    }

    /**
     * Prints the command line help, implemented in the main method.
     */
    public static void help() {
        System.out.print("\nHELP:\n");
        System.out.print("   pos <sentence> - mark every word with its POS tag.\n");
        System.out.print("   chk <sentence> - makes the sentence shallow parsing.\n");
        System.out.print("   prs <sentence> - makes the sentence full parsing.\n\n");
        System.out.print("   for exiting just type return.\n");
    }


    /**
     * Demonstrates the class main features, as well as a small command line for making
     * several operations on sentences, like tagging and parsing. This class uses the
     * 1.5 version of the OpenNLP package, and in order to perform the demonstration,
     * the package must have been installed and their main system path must be supplied
     * in the constructor, for example:
     * <pre>
     *  OpenNLPKit model = new OpenNLPKit("/a/tools/opennlp-tools-1.5.0/models/english/");
     * </pre>
     * If everything is well defined, then the execution of this test will produce the
     * following output:
     * <pre>
     * [X] - LOADING SENTENCE DETECTOR MODEL ... SENTENCE DETECTOR MODEL LOADED
     * [X] - LOADING TOKENIZER MODEL ........... TOKENIZER MODEL LOADED
     * [X] - LOADING TAGGER MODEL .............. TAGGER MODEL LOADED
     * 
     * [NLP KIT GENERAL DEMONSTRATION]
     *
     *
     * [A MULTI-SENTENCE TEXT SEGMENT]
     *    --------------------------------------------
     *    |Yes, said Mr. Heinberg. And it's written  |
     *    |for that audience. It's certainly not     |
     *    |written in such a way that only experts   |
     *    |could benefit from it. A general reader   |
     *    |can easily pick up this $22 book and find |
     *    |their way through it with no problem at   |
     *    |all.                                      |
     *    --------------------------------------------
     *
     *
     * [LIST OF ALL SENTENCES FOUND IN TEXT]
     *    S( 0):... [Yes, said Mr. Heinberg.]
     *    S( 1):... [And it's written for that audience.]
     *    S( 2):... [It's certainly not written in such a way that only experts could benefit from it.]
     *    S( 3):... [A general reader can easily pick up this $22 book and find their way through it with no problem at all.]
     *
     *
     * [PART-OF-SPEECH OF EACH SENTENCE]
     *    S( 0):... [Yes/UH ,/, said/VBD Mr./NNP Heinberg/NNP ./.]
     *    S( 1):... [And/CC it/PRP 's/VBZ written/VBN for/IN that/DT audience/NN ./.]
     *    S( 2):... [It/PRP 's/VBZ certainly/RB not/RB written/VBN in/IN such/JJ a/DT way/NN that/IN only/JJ experts/NNS could/MD benefit/VB from/IN it/PRP ./.]
     *    S( 3):... [A/DT general/JJ reader/NN can/MD easily/RB pick/VB up/RP this/DT $/$ 22/CD book/NN and/CC find/VB their/PRP$ way/NN through/IN it/PRP with/IN no/DT problem/NN at/IN all/DT ./.]
     *
     *
     * [X] - LOADING CHUNKER MODEL ............. CHUNKER MODEL LOADED
     *
     *
     * [SHALLOW PARSING OF EACH SENTENCE]
     *    S( 0):...  [INTJ  Yes/UH ]  ,/, [VP  said/VBD ] [NP  Mr./NNP  Heinberg/NNP ]  ./.
     *    S( 1):...   And/CC [NP  it/PRP ] [VP  's/VBZ  written/VBN ] [PP  for/IN ] [NP  that/DT  audience/NN ]  ./.
     *    S( 2):...  [NP  It/PRP ] [VP  's/VBZ  certainly/RB  not/RB  written/VBN ] [PP  in/IN ] [NP  such/JJ  a/DT  way/NN ] [PP  that/IN ] [NP  only/JJ  experts/NNS ] [VP  could/MD  benefit/VB ] [PP  from/IN ] [NP  it/PRP ]  ./.
     *    S( 3):...  [NP  A/DT  general/JJ  reader/NN ] [VP  can/MD  easily/RB  pick/VB ] [PRT  up/RP ] [NP  this/DT  $/$  22/CD  book/NN ]  and/CC [VP  find/VB ] [NP  their/PRP$  way/NN ] [PP  through/IN ] [NP  it/PRP ] [PP  with/IN ] [NP  no/DT  problem/NN ] [ADVP  at/IN  all/DT ]  ./.
     *
     *
     * [X] - LOADING PARSER MODEL .............. PARSER MODEL LOADED
     *
     *
     * [COMPLETE PARSING OF EACH SENTENCE]
     *    S( 0):... (TOP (S (NP (NNP Yes/UH)) (, ,/,) (VP (VBD said/VBD) (NP (NNP Mr./NNP) (NNP Heinberg/NNP))) (. ./.)))
     *    S( 1):... (TOP (NP (NP (NNP And/CC)) (PP (IN it/PRP) (NP (NP (JJ 's/VBZ) (NN written/VBN)) (PP (IN for/IN) (NP (DT that/DT) (NN audience/NN))))) (. ./.)))
     *    S( 2):... (TOP (S (NP (NP (DT It/PRP) (JJ 's/VBZ) (JJ certainly/RB) (NN not/RB) (NN written/VBN)) (PP (IN in/IN) (NP (NP (JJ such/JJ) (NN a/DT) (NN way/NN)) (PP (IN that/IN) (NP (JJ only/JJ) (NNS experts/NNS)))))) (VP (MD could/MD) (VP (VB benefit/VB) (PP (IN from/IN) (NP (PRP it/PRP))))) (. ./.)))
     *    S( 3):... (TOP (NP (NP (NNP A/DT) (NN general/JJ) (NN reader/NN)) (PP (IN can/MD) (NP (NP (DT easily/RB) (NN pick/VB)) (PP (IN up/RP) (NP (NP (DT this/DT) (JJ $/$) (CD 22/CD) (NN book/NN)) (PP (IN and/CC) (NP (NP (NN find/VB)) (NP (DT their/PRP$) (NN way/NN)) (PP (IN through/IN) (NP (NP (PRP it/PRP)) (PP (IN with/IN) (NP (NP (DT no/DT) (NN problem/NN)) (PP (IN at/IN) (NP (DT all/DT))))))))))))) (. ./.)))
     *
     * input sentence>
     * </pre>
     * The last line is the command line prompt. To know the available commands just type: <b>help</b>.
     * @param args String[]
     */
    public static void main(String[] args) {
        if ( test20171101at17h32() ) return;
        
        //OpenNLPKit model = new OpenNLPKit("/a/tools/opennlp-tools-1.5.0/models/english/");
        OpenNLPKit model = new OpenNLPKit(HULTIGVars.PATH_OPENLP+ "/models/english/");
        //OpenNLPKit model = new OpenNLPKit();
        OSArguments osa= new OSArguments(args);
        if ( osa.contains("modpath") ) {
            model.setModelsPath(osa.get("modpath"));
        }

        model.loadSentenceDetector();
        model.loadTokenizer();
        model.loadTagger();

        System.out.println();
        System.out.println("[NLP KIT GENERAL DEMONSTRATION]\n");
        System.out.println("[A MULTI-SENTENCE TEXT SEGMENT]");
        String txt=
                "Yes, said Mr. Heinberg. And it's written for that audience. It's certainly not written " +
                "in such a way that only experts could benefit from it. A general reader can easily " +
                "pick up this $22 book and find their way through it with no problem at all.";

        System.out.println("   "+Toolkit.sline('-', 44));
        Toolkit.formatedWrite(txt, 42, "   |", "|");
        System.out.println("   "+Toolkit.sline('-', 44));
        System.out.println("\n");

        // THE SENTENCE SPLITTER TEST.
        String[] vs= model.splitSentences(txt);
        System.out.println("[LIST OF ALL SENTENCES FOUND IN TEXT]");
        for (int i = 0; i < vs.length; i++) {
            System.out.printf("   S(%2d):... [%s]\n", i, vs[i]);
        }

        // THE PART-OF-SPEECH TEST.
        System.out.println("\n");
        System.out.println("[PART-OF-SPEECH OF EACH SENTENCE]");
        for (int i = 0; i < vs.length; i++) {
            System.out.printf("   S(%2d):... [%s]\n", i, model.postag(vs[i]));
        }

        // THE CHUNKER TEST.
        System.out.println("\n");
        model.loadChunker();
        System.out.println("\n");
        System.out.println("[SHALLOW PARSING OF EACH SENTENCE]");
        for (int i = 0; i < vs.length; i++) {
            System.out.printf("   S(%2d):... %s\n", i, model.chunk(model.postag(vs[i])));
        }

        // THE PARSER TEST.
        System.out.println("\n");
        model.loadParser();
        System.out.println("\n");
        System.out.println("[COMPLETE PARSING OF EACH SENTENCE]");
        for (int i = 0; i < vs.length; i++) {
            System.out.printf("   S(%2d):... %s\n", i, model.parse(model.postag(vs[i])));
        }

        // A SMALL COMMAND SHELL FOR SENTENCE PROCESSING.
        System.out.println("\n\n");
        System.out.println("--------------------------------------------------");
        System.out.println("SMALL COMMAND SHELL FOR TESTING WITH ANY SENTENCE:");
        System.out.println("--------------------------------------------------");
        System.out.println("for exiting just type return.");
        System.out.println("for help: ? or \"help\"");
        Input in= new Input();
        for (;;) {
            System.out.print("\nINPUT SENTENCE> ");
            String command= in.readLn();
            
            if ( command == null )  break;
            command= command.trim();
            if ( command.length() == 0 ) break;
            
            if ( command.equals("?") || command.equals("help") ) {
                help();
                continue;
            }
            
            int p= command.indexOf(' ');
            if ( p < 0 )  continue;
            String func= command.substring(0,p).trim();
            String sent= command.substring(p+1).trim();
            if (func.equals("pos")) {

                System.out.printf("%s\n", model.postag(sent));
            } else if (func.equals("chk")) {

                String pos = model.postag(sent);
                System.out.printf("%s\n", model.chunk(pos));
            } else if (func.equals("prs") || func.equals("parse")) {
                if (!model.configurationOK(OpenNLPKit.PARSER)) {
                    model.loadParser();
                }
                System.out.printf("%s\n", model.parse(sent));
            }

        }
    }
    
    
    public static boolean test20171101at17h32() {
        String[] v= new String[] {
            "A general reader can easily pick up this $22 book and find their way through it with no problem at all.",
            "It's not easy to understand you!"
                
        };
        CorpusIndex dict= new CorpusIndex();
        for (String s : v ) dict.add(new Sentence(s));
        dict.rebuild();
        
        Sentence s= new Sentence(v[1]);  //s.codify(dict);
        s.print(0,s.size());
        OpenNLPKit onlp= new OpenNLPKit(HULTIGVars.PATH_OPENLP+"/models/english/");
        onlp.loadTokenizer();
        onlp.loadTagger();
        
        for (Word w : s )  System.out.printf("%-20s  %d\n", w.toStringPOS(), w.getLexCod()); 
        System.out.println();
        s= onlp.postag(s);
        for (Word w : s ) System.out.printf("%-20s  %d\n", w.toStringPOS(), w.getLexCod());
        // Depois da etiquetagem, os códigos alteram-se
        
        return true;
    }

}
