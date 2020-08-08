# APL step-by-step

**APL step-by-step** is a work-in-progress interactive environment for exploring the [APL programming language](<https://en.wikipedia.org/wiki/APL_(programming_language)>), with its own implementation of an incomplete subset of APL.

This was built during a mini-retreat at the [Recurse Center](https://www.recurse.com/).

APL is known for its terse but powerful syntax for manipulating multi-dimensional arrays. A powerful notation however can pose a challenge to learning and understanding.

This project tries to create a **learnable environment** for making it easier to understand APL, in hopes of enabling the user to:

- Play around with the language, trying out expressions
- See what the evaluator is doing at each step
- View unfamiliar symbols with human-readable names
- View a side-by-side alternative to APL syntax (S-expressions) that might be easier to understand, and be able to manipulate either one

To enable that, it has the following features:

- REPL (read eval print loop) for evaluating APL expressions
- View each intermediate evaluation step
- Add human-readable names inline to APL symbolic operators
- Convert APL notation into a Lisp-like [S-expression](https://en.wikipedia.org/wiki/S-expression) syntax, and allow the user to bidirectional edit either the APL input or the S-expression input.
  - S-expressions make the order of operations more explicit, so they can be a useful aid for understanding how a programming language in intepreted
- View the current environment (i.e. the assignment of variables to values)

Many of these features can be toggled on and off, allowing the user to learning aids they need.

## Implementation

APL Step-by-step is written in [ClojureScript](https://clojurescript.org/) and uses [Instaparse](https://github.com/Engelberg/instaparse) for parsing and [Reagent](https://github.com/reagent-project/reagent) as a UI library. It uses [cljs.test](https://clojurescript.org/tools/testing) for testing.

In order to support the two alternative syntaxes (APL and S-expression), the project has a parser for both APL (using Instaparse) and S-expressions (using [edn](https://github.com/edn-format/edn) read-string) which produce the same AST (abstract syntax tree) representation which is then evaluated. Each syntax also has a "print" function, which takes the AST and pretty prints it.

## References

- [Try APL](https://tryapl.org/) is an online APL tutorial and REPL. It helped inspire features in this project and was a helpful reference
- [Mastering Dyalog APL](https://www.dyalog.com/uploads/documents/MasteringDyalogAPL.pdf)
- [APL Wiki](https://aplwiki.com/)
- [APL Syntax & Symbols on Wikipedia](https://en.wikipedia.org/wiki/APL_syntax_and_symbols)

## Missing features

- Support for defining functions
- Multi-dimensional array support (i.e. matrices and higher rank, not just vectors and scalars)

## Roadmap

- Add user-defined function and multi-dimensional array support
- Add symbol explanations to interactive environment
- Add more examples
- Add more symbols
- Add more tests
