# SecureNDDTest

### Brief introduction

This is a prototype demonstration that combines LSH (in Hamming distance) and MKSE (multi-key searchable encryption).

### Used libraries:

- JDK-1.8

- JPBC-2.0.0: http://gas.dia.unisa.it/projects/jpbc/#.VlvDdvkrKUk

- OpenCV-2.4.11: http://opencv.org/

- Paillier: http://www.csee.umbc.edu/~kunliu1/research/Paillier.html

- Image Hash: https://pypi.python.org/pypi/ImageHash

### Image dataset:

- INRIA Copydays dataset: http://lear.inrialpes.fr/people/jegou/data.php

- The test data have been processed by using two image hash method, i.e., *average hashing* (abbr. aHash) and *perceptual hashing* (abbr. pHash).

- We use 157 original images as the query set, and expand the remaining subset by generating 9 images with different scale factors for each. Consequently, we have total 14,130 images as our dataset.

### How to use:

- Import the project into Eclipse (I personally used the version Mars). *Double check if the build path is correct or not.*

- Indicated the config file inside the config folder as a running parameter, i.e., PCconfigForTestOnCiphertext.txt or configForTestOnCiphertext.txt. **(Note that you should modify the location of your dataset based on your own settings. You should point the correct pairing setting path (e.g., d159.properties). And you can also try other parameters, like lsh-l, threshold, and "isCached".)**

- To test in plaintext version, just use the setting with "plain". And run **"TestInPlaintext.java"**.

- To test in our secure design, run **"TestPrototypeInCiphertext.java"** for the performance evaluation.

- The **"TestThroughput.java"** is just used for throughput simulation evaluation.

- The **"TestHammingLSH.java"** is an example that shows how to use our implementation of LSH in Hamming distance.

### The folder HammingLSH:

To easily get our implementation of LSH in Hamming distance, I move some related codes from our project **SecureNDDProtypeTest** into this folder.

Note that you should modify some of the "import package" info due to their original locations have been changed.
