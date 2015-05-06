#! /bin/bash

git diff -p --relative a9efb95535d733e48f004d52cb7eee4d92da574e 382caefcc9a4102a715f3ff67dcff9cab80d50b6 client/src/org/kryptose/client/ViewGUI.java > os_bug_fix.patch
git diff -p --relative a9efb95535d733e48f004d52cb7eee4d92da574e 382caefcc9a4102a715f3ff67dcff9cab80d50b6 client/src/org/kryptose/client/Controller.java > other_bug_fix.patch

