# Sharustry
[![Java CI with Gradle](https://github.com/sharlottes/Sharustry/workflows/Java%20CI%20with%20Gradle/badge.svg)](https://github.com/sharlottes/Sharustry/actions) 
[![Discord](https://img.shields.io/discord/704355237246402721.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=7289DA&label=Discord)](https://discord.gg/RCCVQFW)
[![GitHub all releases](https://img.shields.io/github/downloads/anuken/mindustry/total?label=Made%20on%20Mindustry&style=flat-square)](https://github.com/Anuken/Mindustry/)
[![GitHub Repo stars](https://img.shields.io/github/stars/sharlottes/Sharustry?label=Please%20star%20me%21&style=social)](https://github.com/sharlottes/Sharustry/stargazers)

## Releases  

[![Download](https://img.shields.io/github/v/release/sharlottes/Sharustry?color=green&include_prereleases&label=DOWNLOAD%20LATEST%20RELEASE&logo=github&logoColor=green&style=for-the-badge)](https://github.com/sharlottes/Sharustry/releases)
 
Go to the releases, the latest release will have a `dexed-Sharustry.jar` attached to it that you can download. If it does not have it, follow the steps below(recommended) or bother me with a new issue so I can attach the compiled mod.   
After you have the `dexed-Sharustry.jar`, paste it into your mod folder(locate your mod folder in the "open mod folder" of Mindustry).   

Releases를 클릭하고, 최신 버전에 달린 `dexed-Sharustry.jar`를 다운로드하세요. 원하는 버전에 `dexed-Sharustry.jar`가 첨부되어 있지 않다면, 아래의 과정을 따르거나 새로운 Issue로 `.jar`를 달아달라고 저를 괴롭히세요.   
`dexed-Sharustry.jar`를 다운로드 한 후, 모드 디렉토리(민더스트리에서 모드 파일 열기로 확인 가능)에 옮기세요.   

### Actions
[![Java CI with Gradle](https://github.com/sharlottes/Sharustry/workflows/Java%20CI%20with%20Gradle/badge.svg)](https://github.com/sharlottes/Sharustry/actions) 
Unlike the release, Actions can enjoy the mode of **most recent versions** that have recently built commitments. But it's very unstable, and there can be bugs.
Actions 는 릴리즈와는 달리 최근 커밋을 빌드한 **가장 최신 버전**의 모드를 즐기실 수 있습니다. 하지만 매우 불안정적이고, 버그가 있을 수 있습니다.

1. 
Go to [Actions](https://github.com/sharlottes/sharustry/actions), and click on the latest workflow.
[Actions](https://github.com/sharlottes/sharustry/actions) 탭으로 가서, 가장 최근의 Workflow를 클릭하세요. 

![image](https://user-images.githubusercontent.com/60801210/107146488-0d098e00-698c-11eb-82ca-57b1417e627f.png)
![image](https://user-images.githubusercontent.com/60801210/107147102-99698000-698f-11eb-815d-584e4d9b57a8.png)



2. 
Select the "dexed" Artifact (with the box icon), it will download the zip.   
"dexed"이라는 이름의 Artifact(상자 모양 아이콘)을 클릭하면, 압축 파일이 다운로드됩니다.   
![image](https://user-images.githubusercontent.com/60801210/107147181-01b86180-6990-11eb-8a48-bc8e6c477159.png)



3. 
Unzip and paste the `dexed-Sharustry.jar` into your mod folder(locate your mod folder in the "open mod folder" of Mindustry).   
압축 해제 후 `dexed-Sharustry.jar`를 모드 디렉토리(민더스트리에서 모드 파일 열기로 확인 가능)에 옮기세요.  
![image](https://user-images.githubusercontent.com/60801210/107147244-53f98280-6990-11eb-8b48-fda0cd51f80b.png)



4. 
Enjoy!   
끝!

## Compiling / 컴파일하기
translate/copied from https://github.com/Anuken/ExampleJavaMod/edit/master/README.md. you don't need this for playing this mod. notice if you will make java mod.
https://github.com/Anuken/ExampleJavaMod/edit/master/README.md 에서 번역/복사됨. 이 모드를 사용하는데 필요하지 않습니다. 자바 모드를 만들거라면 참고하세요.


JDK 15.

### Building for Desktop Testing / 데스크탑 실험을 위해 빌드하는 방법
1. 
Install JDK 15. If you don't know how, look it up. If you already have any version of the JDK >= 8, that works as well. 
JDK 15를 설치하세요. 

2. 
Run `gradlew jar` [1].
gradlw jar 를 실행하세요.  

3. 
Your mod jar will be in the `build/libs` directory. **Only use this version for testing on desktop. It will not work with Android.**
To build an Android-compatible version, you need the Android SDK. You can either let Github Actions handle this, or set it up yourself. See steps below.
모드 jar파일은 build/libs 에 저장됩니다. **PC 버전에서만 사용하십시오. 안드로이드에선 안될 수 있음.** 
안드로이드 호환 버전을 빌드할려면, 안드로이드 SDK가 필요합니다. 깃허브 Actions에서도 이걸 얻을 수 있습니다. 아래 설명을 확인해주세요.

### Building through Github Actions / 깃헙 Actions 를 통해 빌드하는 방법

This repository is set up with Github Actions CI to automatically build the mod for you every commit. This requires a Github repository, for obvious reasons.
To get a jar file that works for every platform, do the following:
이 레포지토리는 매 커밋마다 자동적으로 빌드하도록 깃헙 Actions CI로 설정되어 있습니다. 이 방법은 당연히 깃허브 레포지토리가 필요합니다.
모든 플렛폼에서 작동하는 jar 파일을 얻기 위해 아래를 따라주세요:

1. 
Make a Github repository with your mod name, and upload the contents of this repo to it. Perform any modifications necessary, then commit and push. 
당신의 모드 이름으로 깃허브 레포지토리를 만드세요, 그리고 이 레포지토리에 당신의 콘텐츠(관련 파일 모두 다)를 업로드하세요. 필요한 수정 작업을 했다면, 커밋을 하고 푸시하면 됩니다.

2. 
Check the "Actions" tab on your repository page. Select the most recent commit in the list. If it completed successfully, there should be a download link under the "Artifacts" section. 
당신의 레포지토리 페이지에서 Actions 탭을 들어간 다음, 목록에서 가장 최근의 커밋을 선택해서 들어가세요. 만약 완벽히 성공했다면, 아래 "Artifacts" 부분에 다운로드 링크가 있어야 합니다. (상자모양)

3. Click the download link (should be the name of your repo). This will download a **zipped jar** - **not** the jar file itself [2]! Unzip this file and import the jar contained within in Mindustry. This version should work both on Android and Desktop.
그 다운로드 링크를 클릭하세요(당신의 레포지토리 이름이여야 함). 그러면 **jar 파일이 아니라 압축된 jar 파일**이 다운로드될 것입니다! 그 파일을 압축해제하고 jar파일을 Mindustry에서 불러오세요.

### Building Locally

Building locally takes more time to set up, but shouldn't be a problem if you've done Android development before.
1. Download the Android SDK, unzip it and set the `ANDROID_HOME` environment variable to its location.
2. Make sure you have API level 30 installed, as well as any recent version of build tools (e.g. 30.0.1)
3. Add a build-tools folder to your PATH. For example, if you have `30.0.1` installed, that would be `$ANDROID_HOME/build-tools/30.0.1`.
4. Run `gradlew deploy`. If you did everything correctlly, this will create a jar file in the `build/libs` directory that can be run on both Android and desktop. 




*[1]* *On Linux/Mac it's `./gradlew`, but if you're using Linux I assume you know how to run executables properly anyway.*  
*[1]* *리눅스나 멕 OS에서는 `./gradlew`입니다.*
*[2]: Yes, I know this is stupid. It's a Github UI limitation - while the jar itself is uploaded unzipped, there is currently no way to download it as a single file.*
*[2]: 네, 저도 이게 바보같은걸 압니다. 이건 Github UI 제한입니다. jar 자체는 압축 해제되어 업로드되지만 현재 단일 파일로 다운로드할 수 있는 방법은 없습니다.*
