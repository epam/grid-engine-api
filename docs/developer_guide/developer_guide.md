### Working on Windows
___
If Windows OS is used for project development, or any other system, which uses line separator, different from `\n`, then the following configurations should be made:
1. After cloning repository to the local machine, please run 
> git config --local core.autocrlf input

in order to tell git not to set default system line separators instead of defined in file from remote repository and let Git fix accidental occurrences of CRLF on commit. 

After this, you'll have to checkout to any other branch and right after that checkout back to current branch to let git replace CRLF with LF.

2. Before creating new template file which is sensitive for line separators (queue creation, etc.), or just after cloning the project, go once to IDEA `File -> Settings -> Editor -> Code Style` and set `Scheme` to `Project` and  `General -> Line separator` to `Unix and macOS (\n)`. That way new files in current project will automatically use desired `\n` separator at future.
