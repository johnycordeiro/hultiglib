# hultiglib
The HULTIG Java Library for text processing.

The HultigLib is a library gathering a set of text processing tools, written in Java language. It was designed for efficiency and scalability, in terms of the volume of text handling. Large collections of texts can be easily computed and used in a variety of applications, as for example main statistics calculations in corpora.

The three core classes of this library are: Word, Sentence, and Text. Their names trivially indicate what they represent. A sentence is represented through a linked list of Word elements. And similarly, a Text is represented by a linked list of Sentence elements. A number of sentence similarity functions can be found in the Sentence, class. These functions have been used in several research works, like paraphrase identification in corpora and plagiarism detection. The Text class also provides several handful tools for representing and processing texts. We have also integrated the "openNLP" library, as well as implemented an "interface" class for using its main relevant features, like part-of-speech tagging, shallow and full sentence parsing.

Original project's webpage: http://www.di.ubi.pt/~jpaulo/hultiglib/
