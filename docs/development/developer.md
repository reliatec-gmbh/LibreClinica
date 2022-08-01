Development Workflows
=====================

Information about LibreClinica development and contribution workflows.

# Version Control

Git version control system is used for tracking changes in LibreClinica
project repositories. Those are hosted publicly via [ReliaTec GmbH
GitHub](https://github.com/reliatec-gmbh/) organisation.

# Repository

[LibreClinica](https://github.com/reliatec-gmbh/LibreClinica) (origin)
repository was created as a fork from
[OpenClinica](https://github.com/OpenClinica/OpenClinica) (upstream)
public repository. Certain naming conventions are defined in order to
prevent naming collisions (for versions, branches and tags).

# Issue Management

GitHub integrated issues management should be used to creates tickets
before any code contributions. Tickets assigned to milestones that
reflect the LibreClinica software versioning scheme. For each version an
appropriate Kanban board is defined to track the progress of development
activities.

# Main Branches

Source code organisation is inspired by Vincent Driessen [git branching
model](https://nvie.com/posts/a-successful-git-branching-model) that
promotes two main branches with an infinite lifetime (master and
lc-develop).

## master

This branch represents the production ready state of code. No direct
commits are permitted to the master. Only merges from supporting
branches (release or hotfix) are allowed. Every changes merged back into
master repository is considered as new release (that shell be tagged).
The initial state of master is cloned from the release branch of
upstream repository (3.14).

## lc-develop

This is a new default branch for origin repository. It tracks
development changes for the next software release. This branch should be
used for the purpose of continuous integration. Changes to lc-develop
are introduced by merges from supporting branches (feature, release or
hotfix).

# Supporting Branches

Additional temporal branches are created from lc-develop or master in
order to introduce changes to the source code.

## feature branches

| Branch from | Merge into | Naming convention                                               |
|-------------|------------|-----------------------------------------------------------------|
| lc-develop  | lc-develop | anything except master, lc-develop, lc-release-\*, lc-hotfix-\* |

Creating a feature branch from lc-develop
``` {.sourceCode .shell}
$ git checkout -b myfeature lc-develop
```
Merging a feature branch on lc-develop
``` {.sourceCode .shell}
$ git checkout lc-develop
$ git merge --no-ff myfeature
$ git branch -d myfeature
$ git push origin lc-develop
```

## release branches

| Branch from | Merge into             | Naming convention |
|-------------|------------------------|-------------------|
| lc-develop  | lc-develop and master  | lc-release-\*     |

Creating a release branch from lc-develop
``` {.sourceCode .shell}
$ git checkout -b lc-release-1.3.0 lc-develop
$ ./bump-version.sh 1.3.0
$ git commit -a -m "Bumped version number to 1.3.0"
```
Merging a release branch on master and tagging
``` {.sourceCode .shell}
$ git checkout master
$ git merge --no-ff lc-release-1.3.0
$ git tag -a lc-1.3.0
```
Merging a release branch on lc-develop
``` {.sourceCode .shell}
$ git checkout lc-develop
$ git merge --no-ff lc-release-1.3.0
```
Removing a temporary release branch
``` {.sourceCode .shell}
$ git branch -d lc-release-1.3.0
```

## hotfix branches

| Branch from |  Merge into           | Naming convention  |
|-------------|-----------------------|--------------------|
 | master      | lc-develop and master | lc-hotfix-\*       |

Creating a hotfix branch from master
``` {.sourceCode .shell}
$ git checkout -b lc-hotfix-1.3.1 master
$ ./bump-version.sh 1.3.1
$ git commit -a -m "Bumped version number to 1.3.1"
```
Fix the bug and commit the fix
``` {.sourceCode .shell}
$ git commit -m "Fixed severe production problem"
```
Merging a hotfix branch on master and tagging
``` {.sourceCode .shell}
$ git checkout master
$ git merge --no-ff lc-hotfix-1.3.1
$ git tag -a lc-1.3.1
```
Merging a hotfix branch on lc-develop
``` {.sourceCode .shell}
$ git checkout lc-develop
$ git merge --no-ff lc-hotfix-1.3.1
```

> **note**
>
> The one exception to the rule here is that, when a release branch
> currently exists, the hotfix changes need to be merged into that
> release branch, instead of develop.

Removing a temporary hotfix branch
``` {.sourceCode .shell}
$ git branch -d lc-hotfix-1.3.1
```

# Tags

Tags should be created for released LibreClinica versions.

# Release Versioning

There is a need to start with fresh versioning scheme for LibreClinica
(independently from upstream) to allow independent release cycle.

-   MAJOR.MINOR.PATCH

Database scheme versioning is currently unchanged. Up until now there
have not been changes in DB scheme introduced in LibreClinica since the
time of fork.

# Contributions

Contributions resolving registered tickets are submitted from personal
forks of developers as pull requests. If the contribution is targeting a
registered bug ticket, this bug need to be described (ideally using
defined bug report template) in a reproducible manner and reproduced by
somebody else from the team of contributors. For new features the
appropriate test specification need to be submitted fitting the numeric
scheme used for test documentation (this should be ideally clarified
with the main contributor team before).
