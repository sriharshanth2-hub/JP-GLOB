# Core Java Banking System

A banking system built in core Java with a Swing GUI and a console version. Built as a GRIET Lab On Board (GLOB) micro project.

## Features
- Create savings and current accounts
- Deposit, withdraw, transfer
- Balance check and mini statement (last 5 transactions)
- Interest for savings accounts, delete account
- Data saved to data.csv and reloaded on startup

## Concepts used
Inheritance, abstraction, polymorphism, encapsulation, interface, collections, file I/O, custom exceptions, Swing event handling

## How to run
javac -d out src/*.java
java -cp out BankingGUI

Console version: java -cp out ConsoleMain

Requires Java 17 or newer.
