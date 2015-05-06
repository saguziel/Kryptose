#! /bin/bash

git diff --relative 382caefcc9a4102a715f3ff67dcff9cab80d50b6 a9efb95535d733e48f004d52cb7eee4d92da574e client/src/org/kryptose/client/ViewGUI.java > os_bug_fix.patch
git diff --relative 382caefcc9a4102a715f3ff67dcff9cab80d50b6 a9efb95535d733e48f004d52cb7eee4d92da574e client/src/org/kryptose/client/Controller.java > other_bug_fix.patch

