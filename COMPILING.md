# Compiling Consoles

### Prerequisites

- You will need several server builds installed to your local repository. You can generate these by running `BuildTools.jar` with the `--rev` argument, followed by the server version. The versions required are:

	- 1.8.3-R0.1-SNAPSHOT (v1_8_R2)

	- 1.8.8-R0.1-SNAPSHOT (v1_8_R3)

	- 1.12.2-R0.1-SNAPSHOT (v1_12_R1)

- You need a maven installation. BuildTools will download one for itself, you can simply add its maven directory to your system path, or install maven normally (on most Linux distributions, you can run `apt-get install maven2`)

### Natives

`consoles-computers` contains C sources that need to be compiled into `libcomputerimpl.so`, which means if you're only interested in the core plugin, you need to remove the `consoles-computers` module from the root `pom.xml`.

If you are on Linux, there are two dependencies, which are libffi and LuaJIT (5.1). These are available on a number of repositories, on Debian/Ubuntu you should be able to run (assuming you are on a 64-bit system):

    sudo apt-get install build-essential libffi6 libluajit-5.1-2 libluajit-5.1-2-dev libluajit-5.1-2:i386 libluajit-5.1-2-dev:i386 libffi6:i386 lib32z1
    
On Arch, there's no multilib version of luajit (so you have to get the i386 version like in Ubuntu if you want 32-bit), and libffi does not install headers into /usr/include (you need to fix this yourself). Otherwise, do:

   sudo pacman -Sy libffi luajit base-devel gdb

for the 64-bit requirements.

### Compiling

The maven configuration should do everything for you; running `mvn install` will generate artifacts in the modules' respective target directories. Consoles will have an usable plugin jar in the `consoles-core/target/final` folder, which has all the dependencies it needs to function.

You can use the builds for bungee straight from the `consoles-bungee/target` folder (it does not require any packaged dependencies), but if you try to use the jars in the `target` folder for other modules (instead of the jars in the `final` folder`), you will be missing a lot of dependencies that don't come with craftbukkit/spigot!
