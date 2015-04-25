#! /bin/bash
#
#Goes to the specified test directory
#
#Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
#Organization: Dept. of MCD Biology, University of Colorado
#record script file
scriptFile=$_
usageParam="-h"
helpParam="--help"
helpMsg="Use $usageParam or $helpParam for more information."
if [ -z "$1" ] ; then
  echo $helpMsg
  if [ $0 == "bash" ] ; then
    return 0
  else
    exit 0
  fi
fi
#constants
fileVers="1.0"
#repository parameters
devParam="D"
compParam="C"
intParam="I"
regParam="R"
branParam="B"
#
versParam="-V"
versParamLong="--version"
#
repNumTag="repository-number"
etomoVersTag="version-string"
usageString="usage: source cdtomo.sh -($devParam|$compParam|$intParam|$regParam|$branParam $etomoVersTag) [$repNumTag]"
#repositories
devName="Development"
compName="Completed"
intName="Integration"
regName="Regression"
branName="Branch"
#variables
ret=
repName=
setImod=
imodRep=
etomoVers=
repNum=
target=
repository=
os=
repositoryPath=
etomoRepositoryPath=
# function setOption
# $1 required
# $2 description
# $3 $0
# $4 current arg - may be empty
# success: ret is assigned $4
# failure: ret is null
setOption ()
{
  ret=
  if [ -n "$4" ] && [[ "$4" != -* ]] ; then
    ret=$4
  elif [ $1 ] ; then
    echo Syntax error: $2 is required.  $helpMsg
    if [ $3 == "bash" ] ; then
      return 1
    else
      exit 1
    fi
  fi
}
# function errCheck
# $1 $?
# $2 $0
errCheck ()
{
  if [ ! $1 -eq 0 ] ; then
    if [ $2 == "bash" ] ; then
      return 0
    else
      exit 0
    fi
  fi
}
while test $# -gt 0; do
  case "$1" in
    $usageParam)
      echo $usageString
      echo
      echo $helpParam for more information
      echo
      if [ $0 == "bash" ] ; then
        return 0
      else
        exit 0
      fi
      ;;
    $helpParam)
      echo Sets up IMOD on Linux or Mac and runs uitests.
      echo
      echo $usageString
      echo
      echo Options:
      echo
      echo -$devParam [$repNumTag]: A $devName repository
      echo -$compParam [$repNumTag]: A $compName repository
      echo -$intParam [$repNumTag]: An $intName repository
      echo -$regParam [$repNumTag]: A $regName repository
      echo -$branParam $etomoVersTag [$repNumTag]: A $branName repository.
      echo
      echo $versParam, $versParamLong: Output script version.
      echo
      echo
      echo Examples:
      echo
      echo Go to the $compName repository:
      echo source cdtomo.sh -C
      echo
      echo "Go to the ${devName}1 repository:"
      echo source cdtomo.sh -D 1
      echo
      echo "Go to the ${branName}1-4.8 repository:"
      echo source cdtomo.sh -B 4.8 1
      echo
      if [ $0 == "bash" ] ; then
        return 0
      else
        exit 0
      fi
      ;;
    -$devParam)
      repName=$devName
      shift
      setOption "" $repNumTag $0 $1
      if [ $ret ] ; then
        repNum=$ret
        break
      fi
      ;;
    -$compParam)
      repName=$compName
      shift
      setOption "" $repNumTag $0 $1
      if [ $ret ] ; then
        repNum=$ret
        break
      fi
      ;;
    -$intParam)
      repName=$intName
      shift
      setOption "" $repNumTag $0 $1
      if [ $ret ] ; then
        repNum=$ret
        break
      fi
      ;;
    -$regParam)
      repName=$regName
      shift
      setOption "" $repNumTag $0 $1
      if [ $ret ] ; then
        repNum=$ret
        break
      fi
      ;;
    -$branParam)
      repName=$branName
      shift
      setOption true $etomoVersTag $0 $1
      etomoVers=$ret
      shift
      setOption "" $repNumTag $0 $1
      if [ $ret ] ; then
        repNum=$ret
        break
      fi
      ;;
    $versParam)
      echo $fileVers
      shift
      ;;
    $versParamLong)
      echo $fileVers
      shift
      ;;
    *)
      echo Syntax error at \"$1\".  $helpMsg
      if [ $0 == "bash" ] ; then
        return 1
      else
        exit 1
      fi
      ;;
  esac
done
#set repository
if [ -n "$repName" ] ; then
  if [ "$repName" == $branName ] ; then
    repository=$repName$repNum-$etomoVers
  else
    repository=$repName$repNum
  fi
  echo $repository
  cd $HOME/workspace/$repository
fi
