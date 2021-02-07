# Sharustry
[![Java CI with Gradle](https://github.com/sharlottes/Sharustry/workflows/Java%20CI%20with%20Gradle/badge.svg)](https://github.com/sharlottes/Sharustry/actions) 
[![Discord](https://img.shields.io/discord/704355237246402721.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=7289DA&label=Discord)](https://discord.gg/RCCVQFW)
[![GitHub all releases](https://img.shields.io/github/downloads/anuken/mindustry/total?label=Made%20on%20Mindustry&style=flat-square)](https://github.com/Anuken/Mindustry/)
[![GitHub Repo stars](https://img.shields.io/github/stars/sharlottes/Sharustry?label=Please%20star%20me%21&style=social)](https://github.com/sharlottes/Sharustry/stargazers)


[![Download](https://img.shields.io/github/v/release/sharlottes/Sharustry?color=green&include_prereleases&label=DOWNLOAD%20LATEST%20RELEASE&logo=github&logoColor=green&style=for-the-badge)](https://github.com/sharlottes/Sharustry/releases)

## Releases   
Go to the releases, the latest release will have a `Sharustry.jar` attached to it that you can download. If it does not have it, follow the steps below(recommended) or bother me with a new issue so I can attach the compiled mod.   
After you have the `Sharustry.jar`, paste it into your mod folder(locate your mod folder in the "open mod folder" of Mindustry).   

Releases를 클릭하고, 최신 버전에 달린 `BetaMindy.jar`를 다운로드하세요. 원하는 버전에 `Sharustry.jar`가 첨부되어 있지 않다면, 아래의 과정을 따르거나 새로운 Issue로 `.jar`를 달아달라고 저를 괴롭히세요.   
`Sharustry.jar`를 다운로드 한 후, 모드 디렉토리(민더스트리에서 모드 파일 열기로 확인 가능)에 옮기세요.   

## Compiling
JDK 15.

1. Install JDK 14. If you don't know how, look it up. If you already have any version of the JDK >= 8, that works as well. 
2. Run `gradlew jar` [1].
3. Your mod jar will be in the `build/libs` directory. **Only use this version for testing on desktop. It will not work with Android.**
To build an Android-compatible version, you need the Android SDK. You can either let Github Actions handle this, or set it up yourself. See steps below.

*[1]* *On Linux/Mac it's `./gradlew`, but if you're using Linux I assume you know how to run executables properly anyway.*  
