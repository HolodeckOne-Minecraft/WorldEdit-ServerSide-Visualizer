#!/bin/bash

if [ "$TRAVIS_JDK_VERSION" == "oraclejdk7" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ] ; then

  echo -e "Generating javadoc...\n"
  if [ -e ./gradlew ]; then ./gradlew Javadoc;else gradle Javadoc; fi

  echo -e "Publishing javadoc...\n"

  cp -R build/com.rojel.wesv/docs/javadoc $HOME/javadoc-latest
  /bin/cp -f README.md $HOME/README.md

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/martinambrus/WorldEdit-ServerSide-Visualizer gh-pages > /dev/null

  cd gh-pages
  git rm -rf ./javadoc
  git rm -rf ./README.md
  cp -Rf $HOME/javadoc-latest ./javadoc
  /bin/cp -f $HOME/README.md .
  git add -f .
  git commit -m "Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Published Javadoc to gh-pages.\n"

fi