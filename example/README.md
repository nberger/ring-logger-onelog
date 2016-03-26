# ring-logger-onelog example project

A Clojure program designed to show how to use ring-logger-onelog and the
output that it produces

## Usage

1. `lein trampoline run`

2. Check output in console *and* in `log/clojure.log` (the default output file
   configured by onelog). The output is generated twice, the first one with the
   default colorizing printer, the second one with `:printer :no-color`.


## License

Copyright © 2016 Nicolás Berger

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
