# SOEN342

## Team Information

- **Team Members:**
  - Anthony Lim — Student ID: [40281180] anthonylim459@hotmail.com (**Team Lead**)  
  - Victor Taing — Student ID: [40276829] vic3.taing@gmail.com
  - Andrew Ungureanu — Student ID: [40283344] andrewrazvanu@gmail.com


  # EU Rail Network Search System

A Java command-line application for searching train routes across Europe with support for direct routes and multi-leg connections.

## Prerequisites

- Java JDK 11 or higher
- CSV data file at `src/data/eu_rail_network.csv`

## Installation

```bash
git clone <your-repository-url>
cd <project-directory>
```

## Running the Application

### Command Line

```bash
# Compile
javac -d bin src/**/*.java src/*.java

# Run
java -cp bin Main
```

### IDE (IntelliJ / Eclipse / VS Code)

1. Open the project folder in your IDE
2. Run `Main.java`

## Usage

The application prompts for search criteria:

- **Departure/Arrival city** - Required for connections
- **Train type** - e.g., TGV, ICE, AVE
- **Day of operation** - Mon/Tue/Wed/Thu/Fri/Sat/Sun/Daily
- **Max prices** - First and second class (€)
- **Departure time range** - HH:MM format
- **Max duration** - Minutes per leg

Leave any field blank to skip that filter.

If no direct routes are found, the system searches for multi-leg connections with transfer options.

## Example

```
Route search:

Departure city: Paris
Arrival city: Berlin
Train type: 
Day of operation: Mon
Max 1st class price (€): 150
Max 2nd class price (€): 100
Min departure time (HH:MM): 08:00
Max departure time (HH:MM): 20:00
Max duration (minutes): 600
Min transfer minutes : 30
Max connections: 2

--- Search Results ---
Choose a sorting option:
0
--- Search Results Sorted ---
Book one of the following trips: y
Enter route # to book (1-1): 1
Number of travellers: 2 
Traveller 1:
  First name: Anthony
  Last  name: Lim
  Age: 22
  ID (passport/gov): 1234 
Traveller 2:
  First name: Andrew
  Last  name: Ungureanu
  Age: 21
  ID (passport/gov): 12345 
  

View my trips:
Enter your last name: Lim
Enter your ID (passport/gov): 1234

```

## Features

- Direct route search with comprehensive filtering
- Multi-leg connections (up to 2 transfers)
- Sort by time, duration, or price
- Automatic day transition handling
- Transfer time validation
- View my trips
- No Layovers during night (10pm-6am)

Demo video:
https://youtu.be/Z8Uq6ZcrxOE