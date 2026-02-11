#!/usr/bin/env Rscript
# Pointcut Comparison Tool
# Reads pointcuts from multiple sources and merges them into one CSV

library(stringr)

# ============================================================================
# Configuration
# ============================================================================

ARES_ROOT <- "/Users/markuspaulsen/Documents/Ares2"

INSTRUMENTATION_FILE <- file.path(ARES_ROOT, 
  "src/main/java/de/tum/cit/ase/ares/api/aop/java/instrumentation/pointcut/JavaInstrumentationPointcutDefinitions.java")

ASPECTJ_FILE <- file.path(ARES_ROOT,
  "src/main/java/de/tum/cit/ase/ares/api/aop/java/aspectj/adviceandpointcut/JavaAspectJFileSystemPointcutDefinitions.aj")

ARCHUNIT_FILE <- file.path(ARES_ROOT,
  "src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/file-system-access-methods.txt")

WALA_FILE <- file.path(ARES_ROOT,
  "src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/file-system-access-methods.txt")

DOCUMENTATION_FILE <- file.path(ARES_ROOT,
  "docs/aop/BlockFileSystemAccessAOP.md")

ARCHITECTURE_DOCUMENTATION_FILE <- file.path(ARES_ROOT,
  "docs/architecture/BlockFileSystemAccessArchitecture.md")

OUTPUT_FILE <- file.path(ARES_ROOT, "tools/pointcut_comparison.csv")

# ============================================================================
# Helper Functions
# ============================================================================

# Map action keywords to standardized action names
map_action <- function(method_name) {
  if (grepl("Read|read", method_name)) return("READ")
  if (grepl("Overwrite|Write|write", method_name)) return("OVERWRITE")
  if (grepl("Execute|execute", method_name)) return("EXECUTE")
  if (grepl("Delete|delete", method_name)) return("DELETE")
  if (grepl("Create|create", method_name)) return("CREATE")
  return("UNKNOWN")
}

# ============================================================================
# 1. Parse Java Instrumentation Pointcut Definitions
# ============================================================================

parse_instrumentation <- function(filepath) {
  content <- readLines(filepath, warn = FALSE)
  content <- paste(content, collapse = "\n")
  
  # Define which maps to extract and their actions
  maps <- list(
    list(name = "methodsWhichCanReadFiles", action = "READ"),
    list(name = "methodsWhichCanOverwriteFiles", action = "OVERWRITE"),
    list(name = "methodsWhichCanExecuteFiles", action = "EXECUTE"),
    list(name = "methodsWhichCanDeleteFiles", action = "DELETE"),
    list(name = "methodsWhichCanCreateFiles", action = "CREATE")
  )
  
  results <- data.frame(
    class = character(),
    method = character(),
    action = character(),
    source = character(),
    stringsAsFactors = FALSE
  )
  
  for (map_info in maps) {
    # Find the map definition
    pattern <- paste0(map_info$name, "\\s*=\\s*Map\\.ofEntries\\(([^;]+?)\\);")
    match <- str_match(content, pattern)
    
    if (!is.na(match[1])) {
      map_content <- match[2]
      
      # Extract Map.entry pairs
      entry_pattern <- 'Map\\.entry\\("([^"]+)"\\s*,\\s*List\\.of\\(([^)]+)\\)'
      entries <- str_match_all(map_content, entry_pattern)[[1]]
      
      if (nrow(entries) > 0) {
        for (i in 1:nrow(entries)) {
          class_name <- entries[i, 2]
          methods_str <- entries[i, 3]
          
          # Extract method names from the List.of(...)
          method_pattern <- '"([^"]+)"'
          methods <- str_match_all(methods_str, method_pattern)[[1]]
          
          if (nrow(methods) > 0) {
            for (j in 1:nrow(methods)) {
              method_name <- methods[j, 2]
              results <- rbind(results, data.frame(
                class = class_name,
                method = method_name,
                action = map_info$action,
                source = "INSTRUMENTATION",
                stringsAsFactors = FALSE
              ))
            }
          }
        }
      }
    }
  }
  
  return(results)
}

# ============================================================================
# 2. Parse AspectJ Pointcut Definitions
# ============================================================================

parse_aspectj <- function(filepath) {
  content <- readLines(filepath, warn = FALSE)
  content <- paste(content, collapse = "\n")
  
  results <- data.frame(
    class = character(),
    method = character(),
    action = character(),
    source = character(),
    stringsAsFactors = FALSE
  )
  
  # Find all pointcut definitions
  # Pattern: pointcut someName(): (call(...) || call(...) || ...);
  pointcut_pattern <- "pointcut\\s+(\\w+)\\s*\\(\\s*\\)\\s*:\\s*([^;]+);"
  pointcuts <- str_match_all(content, pointcut_pattern)[[1]]
  
  if (nrow(pointcuts) > 0) {
    for (i in 1:nrow(pointcuts)) {
      pointcut_name <- pointcuts[i, 2]
      pointcut_body <- pointcuts[i, 3]
      
      # Skip if(false) pointcuts
      if (grepl("if\\s*\\(\\s*false\\s*\\)", pointcut_body)) next
      
      # Determine action from pointcut name
      action <- map_action(pointcut_name)
      
      # Extract call patterns: call(* package.Class.method(..)) or call(package.Class.new(..))
      # Pattern for methods: call(* fully.qualified.Class.method(..))
      call_pattern <- "call\\s*\\(\\s*(?:\\*\\s+)?([a-zA-Z0-9_.$+]+)\\.([a-zA-Z0-9_<>]+)\\s*\\(\\.\\.\\.?\\)\\s*\\)"
      calls <- str_match_all(pointcut_body, call_pattern)[[1]]
      
      if (nrow(calls) > 0) {
        for (j in 1:nrow(calls)) {
          class_name <- gsub("\\+$", "", calls[j, 2])
          method_name <- calls[j, 3]
          
          # Convert "new" to "<init>"
          if (method_name == "new") method_name <- "<init>"
          
          results <- rbind(results, data.frame(
            class = class_name,
            method = method_name,
            action = action,
            source = "ASPECTJ",
            stringsAsFactors = FALSE
          ))
        }
      }
    }
  }
  
  return(results)
}

# ============================================================================
# 3. Parse ArchUnit/WALA Method Files
# ============================================================================

parse_method_file <- function(filepath, source_name) {
  lines <- readLines(filepath, warn = FALSE)
  lines <- lines[lines != ""]  # Remove empty lines
  
  results <- data.frame(
    class = character(),
    method = character(),
    action = character(),
    source = character(),
    stringsAsFactors = FALSE
  )
  
  for (line in lines) {
    # Format: package.Class.method(params) or package.Class.<init>(params)
    # Extract class and method
    
    # Remove parameters for simpler parsing
    line_no_params <- sub("\\(.*$", "", line)
    
    # Split by last dot to get class and method
    parts <- strsplit(line_no_params, "\\.")[[1]]
    if (length(parts) >= 2) {
      method_name <- parts[length(parts)]
      class_name <- paste(parts[-length(parts)], collapse = ".")
      
      results <- rbind(results, data.frame(
        class = class_name,
        method = method_name,
        action = NA,  # No action info in these files
        source = source_name,
        stringsAsFactors = FALSE
      ))
    }
  }
  
  return(results)
}

# ============================================================================
# 4. Parse Documentation (BlockFileSystemAccessAOP.md)
# ============================================================================

parse_documentation <- function(filepath) {
  lines <- readLines(filepath, warn = FALSE)
  
  results <- data.frame(
    class = character(),
    method = character(),
    action = character(),
    aspectj_doc = character(),
    bytebuddy_doc = character(),
    source = character(),
    stringsAsFactors = FALSE
  )
  
  current_action <- NA
  
  for (i in 1:length(lines)) {
    line <- lines[i]
    
    # Detect section headers for actions
    if (grepl("^## 2\\.2 What Are The Monitored READ Operations", line)) {
      current_action <- "READ"
    } else if (grepl("^## 2\\.3 What Are The Monitored WRITE Operations", line) ||
               grepl("^## 2\\.3 What Are The Monitored OVERWRITE Operations", line)) {
      current_action <- "OVERWRITE"
    } else if (grepl("^## 2\\.4 What Are The Monitored CREATE Operations", line)) {
      current_action <- "CREATE"
    } else if (grepl("^## 2\\.5 What Are The Monitored DELETE Operations", line)) {
      current_action <- "DELETE"
    } else if (grepl("^## 2\\.6 What Are The Monitored EXECUTE Operations", line)) {
      current_action <- "EXECUTE"
    } else if (grepl("^## 3\\.", line) || grepl("^# 3\\.", line)) {
      # End of monitored operations sections
      current_action <- NA
    }
    
    # Parse table rows: | class | method | AspectJ | ByteBuddy | Tested |
    if (!is.na(current_action) && grepl("^\\|\\s*[a-z]", line)) {
      # Split by |
      parts <- strsplit(line, "\\|")[[1]]
      parts <- trimws(parts)
      parts <- parts[parts != ""]
      
      if (length(parts) >= 4) {
        class_name <- parts[1]
        method_name <- parts[2]
        aspectj_status <- parts[3]
        bytebuddy_status <- parts[4]
        
        # Clean up method name (remove backticks)
        method_name <- gsub("`", "", method_name)
        # Convert <new> to <init>
        if (method_name == "<new>") method_name <- "<init>"
        
        # Clean up status (extract just the emoji or status)
        # Check for "via" pattern which means indirect control
        if (grepl("via", aspectj_status, ignore.case = TRUE)) {
          aspectj_clean <- "NO (via)"
        } else if (grepl("✅", aspectj_status)) {
          aspectj_clean <- "YES"
        } else if (grepl("❌", aspectj_status)) {
          aspectj_clean <- "NO"
        } else {
          aspectj_clean <- "UNKNOWN"
        }
        
        if (grepl("via", bytebuddy_status, ignore.case = TRUE)) {
          bytebuddy_clean <- "NO (via)"
        } else if (grepl("✅", bytebuddy_status)) {
          bytebuddy_clean <- "YES"
        } else if (grepl("❌", bytebuddy_status)) {
          bytebuddy_clean <- "NO"
        } else {
          bytebuddy_clean <- "UNKNOWN"
        }
        
        results <- rbind(results, data.frame(
          class = class_name,
          method = method_name,
          action = current_action,
          aspectj_doc = aspectj_clean,
          bytebuddy_doc = bytebuddy_clean,
          source = "DOCUMENTATION",
          stringsAsFactors = FALSE
        ))
      }
    }
  }
  
  return(results)
}

# ============================================================================
# 5. Parse Architecture Documentation (BlockFileSystemAccessArchitecture.md)
# ============================================================================

parse_architecture_documentation <- function(filepath) {
  lines <- readLines(filepath, warn = FALSE)
  
  results <- data.frame(
    class = character(),
    method = character(),
    action = character(),
    archunit_doc = character(),
    wala_doc = character(),
    source = character(),
    stringsAsFactors = FALSE
  )
  
  current_action <- NA
  
  for (i in 1:length(lines)) {
    line <- lines[i]
    
    # Detect section headers for actions
    if (grepl("^## 2\\.2 What Are The Monitored READ Operations", line)) {
      current_action <- "READ"
    } else if (grepl("^## 2\\.3 What Are The Monitored WRITE", line) ||
               grepl("^## 2\\.3 What Are The Monitored OVERWRITE", line)) {
      current_action <- "OVERWRITE"
    } else if (grepl("^## 2\\.4 What Are The Monitored CREATE Operations", line)) {
      current_action <- "CREATE"
    } else if (grepl("^## 2\\.5 What Are The Monitored DELETE Operations", line)) {
      current_action <- "DELETE"
    } else if (grepl("^## 2\\.6 What Are The Monitored EXECUTE Operations", line)) {
      current_action <- "EXECUTE"
    } else if (grepl("^## 3\\.", line) || grepl("^# 3\\.", line)) {
      # End of monitored operations sections
      current_action <- NA
    }
    
    # Parse table rows: | class | method | ArchUnit | WALA | Tested |
    if (!is.na(current_action) && grepl("^\\|\\s*[a-z]", line)) {
      # Split by |
      parts <- strsplit(line, "\\|")[[1]]
      parts <- trimws(parts)
      parts <- parts[parts != ""]
      
      if (length(parts) >= 4) {
        class_name <- parts[1]
        method_name <- parts[2]
        archunit_status <- parts[3]
        wala_status <- parts[4]
        
        # Clean up method name (remove backticks)
        method_name <- gsub("`", "", method_name)
        # Convert <new> to <init>
        if (method_name == "<new>") method_name <- "<init>"
        
        # Clean up status (extract just the emoji or status)
        if (grepl("✅", archunit_status)) {
          archunit_clean <- "YES"
        } else if (grepl("❌", archunit_status)) {
          archunit_clean <- "NO"
        } else {
          archunit_clean <- "UNKNOWN"
        }
        
        if (grepl("✅", wala_status)) {
          wala_clean <- "YES"
        } else if (grepl("❌", wala_status)) {
          wala_clean <- "NO"
        } else {
          wala_clean <- "UNKNOWN"
        }
        
        results <- rbind(results, data.frame(
          class = class_name,
          method = method_name,
          action = current_action,
          archunit_doc = archunit_clean,
          wala_doc = wala_clean,
          source = "ARCH_DOCUMENTATION",
          stringsAsFactors = FALSE
        ))
      }
    }
  }
  
  return(results)
}

# ============================================================================
# Main Execution
# ============================================================================

cat("Parsing Instrumentation Pointcuts...\n")
instr_df <- parse_instrumentation(INSTRUMENTATION_FILE)
cat(sprintf("  Found %d entries\n", nrow(instr_df)))

cat("Parsing AspectJ Pointcuts...\n")
aspectj_df <- parse_aspectj(ASPECTJ_FILE)
cat(sprintf("  Found %d entries\n", nrow(aspectj_df)))

cat("Parsing ArchUnit Methods...\n")
archunit_df <- parse_method_file(ARCHUNIT_FILE, "ARCHUNIT")
cat(sprintf("  Found %d entries\n", nrow(archunit_df)))

cat("Parsing WALA Methods...\n")
wala_df <- parse_method_file(WALA_FILE, "WALA")
cat(sprintf("  Found %d entries\n", nrow(wala_df)))

cat("Parsing Documentation...\n")
doc_df <- parse_documentation(DOCUMENTATION_FILE)
cat(sprintf("  Found %d entries\n", nrow(doc_df)))

cat("Parsing Architecture Documentation...\n")
arch_doc_df <- parse_architecture_documentation(ARCHITECTURE_DOCUMENTATION_FILE)
cat(sprintf("  Found %d entries\n", nrow(arch_doc_df)))

# ============================================================================
# Merge into unified table
# ============================================================================

cat("\nMerging results...\n")

# Create unique key for each class+method combination from all sources
# Add doc_df (without the extra columns) to the combined sources
doc_simple <- doc_df[, c("class", "method", "action", "source")]
arch_doc_simple <- arch_doc_df[, c("class", "method", "action", "source")]
all_sources <- rbind(instr_df, aspectj_df, archunit_df, wala_df, doc_simple, arch_doc_simple)

# Pivot to wide format
unique_methods <- unique(all_sources[, c("class", "method")])

merged <- unique_methods
merged$INSTRUMENTATION <- NA
merged$ASPECTJ <- NA
merged$DOCUMENTATION <- NA
merged$DOC_ASPECTJ <- NA
merged$DOC_BYTEBUDDY <- NA
merged$ARCHUNIT <- FALSE
merged$WALA <- FALSE
merged$ARCH_DOCUMENTATION <- NA
merged$DOC_ARCHUNIT <- NA
merged$DOC_WALA <- NA

for (i in 1:nrow(merged)) {
  cls <- merged$class[i]
  mth <- merged$method[i]
  
  # Check Instrumentation
  instr_match <- instr_df[instr_df$class == cls & instr_df$method == mth, ]
  if (nrow(instr_match) > 0) {
    merged$INSTRUMENTATION[i] <- paste(unique(instr_match$action), collapse = ",")
  }
  
  # Check AspectJ
  aspectj_match <- aspectj_df[aspectj_df$class == cls & aspectj_df$method == mth, ]
  if (nrow(aspectj_match) > 0) {
    merged$ASPECTJ[i] <- paste(unique(aspectj_match$action), collapse = ",")
  }
  
  # Check Documentation (AOP)
  doc_match <- doc_df[doc_df$class == cls & doc_df$method == mth, ]
  if (nrow(doc_match) > 0) {
    merged$DOCUMENTATION[i] <- paste(unique(doc_match$action), collapse = ",")
    merged$DOC_ASPECTJ[i] <- paste(unique(doc_match$aspectj_doc), collapse = ",")
    merged$DOC_BYTEBUDDY[i] <- paste(unique(doc_match$bytebuddy_doc), collapse = ",")
  }
  
  # Check ArchUnit
  merged$ARCHUNIT[i] <- any(archunit_df$class == cls & archunit_df$method == mth)
  
  # Check WALA
  merged$WALA[i] <- any(wala_df$class == cls & wala_df$method == mth)
  
  # Check Architecture Documentation
  arch_doc_match <- arch_doc_df[arch_doc_df$class == cls & arch_doc_df$method == mth, ]
  if (nrow(arch_doc_match) > 0) {
    merged$ARCH_DOCUMENTATION[i] <- paste(unique(arch_doc_match$action), collapse = ",")
    merged$DOC_ARCHUNIT[i] <- paste(unique(arch_doc_match$archunit_doc), collapse = ",")
    merged$DOC_WALA[i] <- paste(unique(arch_doc_match$wala_doc), collapse = ",")
  }
}

# Sort by class and method
merged <- merged[order(merged$class, merged$method), ]

# Write to CSV
write.csv(merged, OUTPUT_FILE, row.names = FALSE)
cat(sprintf("\nResults written to: %s\n", OUTPUT_FILE))
cat(sprintf("Total unique class+method combinations: %d\n", nrow(merged)))

# Print summary
cat("\n=== Summary ===\n")
cat(sprintf("Methods in INSTRUMENTATION: %d\n", sum(!is.na(merged$INSTRUMENTATION))))
cat(sprintf("Methods in ASPECTJ: %d\n", sum(!is.na(merged$ASPECTJ))))
cat(sprintf("Methods in DOCUMENTATION (AOP): %d\n", sum(!is.na(merged$DOCUMENTATION))))
cat(sprintf("Methods in ARCHUNIT: %d\n", sum(merged$ARCHUNIT)))
cat(sprintf("Methods in WALA: %d\n", sum(merged$WALA)))
cat(sprintf("Methods in ARCH_DOCUMENTATION: %d\n", sum(!is.na(merged$ARCH_DOCUMENTATION))))

# ============================================================================
# Show ALL discrepancies (all 20 directions for 5 sources)
# ============================================================================

cat("\n=== ALL Discrepancies (20 directions) ===\n")

# Helper function to print discrepancies
print_discrepancies <- function(df, source_a, source_b, col_a, col_b, max_items = 5) {
  if (col_a %in% c("ARCHUNIT", "WALA")) {
    in_a <- df[[col_a]] == TRUE
  } else {
    in_a <- !is.na(df[[col_a]])
  }
  
  if (col_b %in% c("ARCHUNIT", "WALA")) {
    in_b <- df[[col_b]] == TRUE
  } else {
    in_b <- !is.na(df[[col_b]])
  }
  
  only_a <- df[in_a & !in_b, ]
  count <- nrow(only_a)
  
  cat(sprintf("\n%s → %s (%d in %s but not %s):\n", source_a, source_b, count, source_a, source_b))
  
  if (count == 0) {
    cat("  (none)\n")
  } else {
    for (i in 1:min(max_items, count)) {
      action <- ""
      if (!col_a %in% c("ARCHUNIT", "WALA") && !is.na(only_a[[col_a]][i])) {
        action <- sprintf(" [%s]", only_a[[col_a]][i])
      }
      cat(sprintf("  %s.%s%s\n", only_a$class[i], only_a$method[i], action))
    }
    if (count > max_items) cat(sprintf("  ... and %d more\n", count - max_items))
  }
  
  return(count)
}

cat("\n=== ALL Discrepancies (30 directions) ===\n")

# Define all 7 sources (added ARCH_DOCUMENTATION)
sources <- list(
  list(name = "INSTRUMENTATION", col = "INSTRUMENTATION"),
  list(name = "ASPECTJ", col = "ASPECTJ"),
  list(name = "DOCUMENTATION", col = "DOCUMENTATION"),
  list(name = "ARCHUNIT", col = "ARCHUNIT"),
  list(name = "WALA", col = "WALA"),
  list(name = "ARCH_DOCUMENTATION", col = "ARCH_DOCUMENTATION")
)

# Generate all 30 comparisons (6 * 5 = 30)
total_discrepancies <- 0
discrepancy_matrix <- matrix(0, nrow = 6, ncol = 6)
rownames(discrepancy_matrix) <- sapply(sources, function(s) s$name)
colnames(discrepancy_matrix) <- sapply(sources, function(s) s$name)

for (i in 1:length(sources)) {
  for (j in 1:length(sources)) {
    if (i != j) {
      count <- print_discrepancies(
        merged, 
        sources[[i]]$name, 
        sources[[j]]$name, 
        sources[[i]]$col, 
        sources[[j]]$col
      )
      discrepancy_matrix[i, j] <- count
      total_discrepancies <- total_discrepancies + count
    }
  }
}

# Print summary matrix
cat("\n=== Discrepancy Matrix ===\n")
cat("(Row has X entries that Column doesn't have)\n\n")
cat(sprintf("%20s", ""))
for (name in colnames(discrepancy_matrix)) {
  cat(sprintf(" %12s", substr(name, 1, 12)))
}
cat("\n")
for (i in 1:nrow(discrepancy_matrix)) {
  cat(sprintf("%20s", rownames(discrepancy_matrix)[i]))
  for (j in 1:ncol(discrepancy_matrix)) {
    if (i == j) {
      cat(sprintf(" %12s", "-"))
    } else {
      cat(sprintf(" %12d", discrepancy_matrix[i, j]))
    }
  }
  cat("\n")
}

cat(sprintf("\nTotal discrepancies across all 30 directions: %d\n", total_discrepancies))

# ============================================================================
# Check Documentation Accuracy (DOC_ASPECTJ, DOC_BYTEBUDDY, DOC_ARCHUNIT, DOC_WALA correctness)
# ============================================================================

cat("\n=== Documentation Accuracy Check ===\n")
cat("(Verifying DOC_ASPECTJ, DOC_BYTEBUDDY, DOC_ARCHUNIT, and DOC_WALA columns match actual code)\n")

doc_errors <- data.frame(
  class = character(),
  method = character(),
  column = character(),
  doc_value = character(),
  expected = character(),
  stringsAsFactors = FALSE
)

for (i in 1:nrow(merged)) {
  cls <- merged$class[i]
  mth <- merged$method[i]
  
  # Check AOP Documentation (DOC_ASPECTJ and DOC_BYTEBUDDY)
  if (!is.na(merged$DOCUMENTATION[i])) {
    # Check DOC_ASPECTJ accuracy
    in_aspectj <- !is.na(merged$ASPECTJ[i])
    doc_aspectj <- merged$DOC_ASPECTJ[i]
    
    if (!is.na(doc_aspectj) && !grepl("via", doc_aspectj, ignore.case = TRUE)) {
      expected_aspectj <- if (in_aspectj) "YES" else "NO"
      if (doc_aspectj != expected_aspectj) {
        doc_errors <- rbind(doc_errors, data.frame(
          class = cls,
          method = mth,
          column = "DOC_ASPECTJ",
          doc_value = doc_aspectj,
          expected = expected_aspectj,
          stringsAsFactors = FALSE
        ))
      }
    }
    
    # Check DOC_BYTEBUDDY accuracy
    in_instrumentation <- !is.na(merged$INSTRUMENTATION[i])
    doc_bytebuddy <- merged$DOC_BYTEBUDDY[i]
    
    if (!is.na(doc_bytebuddy) && !grepl("via", doc_bytebuddy, ignore.case = TRUE)) {
      expected_bytebuddy <- if (in_instrumentation) "YES" else "NO"
      if (doc_bytebuddy != expected_bytebuddy) {
        doc_errors <- rbind(doc_errors, data.frame(
          class = cls,
          method = mth,
          column = "DOC_BYTEBUDDY",
          doc_value = doc_bytebuddy,
          expected = expected_bytebuddy,
          stringsAsFactors = FALSE
        ))
      }
    }
  }
  
  # Check Architecture Documentation (DOC_ARCHUNIT and DOC_WALA)
  if (!is.na(merged$ARCH_DOCUMENTATION[i])) {
    # Check DOC_ARCHUNIT accuracy
    in_archunit <- merged$ARCHUNIT[i]
    doc_archunit <- merged$DOC_ARCHUNIT[i]
    
    if (!is.na(doc_archunit)) {
      expected_archunit <- if (in_archunit) "YES" else "NO"
      if (doc_archunit != expected_archunit) {
        doc_errors <- rbind(doc_errors, data.frame(
          class = cls,
          method = mth,
          column = "DOC_ARCHUNIT",
          doc_value = doc_archunit,
          expected = expected_archunit,
          stringsAsFactors = FALSE
        ))
      }
    }
    
    # Check DOC_WALA accuracy
    in_wala <- merged$WALA[i]
    doc_wala <- merged$DOC_WALA[i]
    
    if (!is.na(doc_wala)) {
      expected_wala <- if (in_wala) "YES" else "NO"
      if (doc_wala != expected_wala) {
        doc_errors <- rbind(doc_errors, data.frame(
          class = cls,
          method = mth,
          column = "DOC_WALA",
          doc_value = doc_wala,
          expected = expected_wala,
          stringsAsFactors = FALSE
        ))
      }
    }
  }
}

if (nrow(doc_errors) == 0) {
  cat("\n✅ All documentation markers are correct!\n")
} else {
  cat(sprintf("\n❌ Found %d documentation errors:\n\n", nrow(doc_errors)))
  
  # Group by column
  aspectj_errors <- doc_errors[doc_errors$column == "DOC_ASPECTJ", ]
  bytebuddy_errors <- doc_errors[doc_errors$column == "DOC_BYTEBUDDY", ]
  archunit_errors <- doc_errors[doc_errors$column == "DOC_ARCHUNIT", ]
  wala_errors <- doc_errors[doc_errors$column == "DOC_WALA", ]
  
  if (nrow(aspectj_errors) > 0) {
    cat(sprintf("DOC_ASPECTJ errors (%d):\n", nrow(aspectj_errors)))
    for (i in 1:nrow(aspectj_errors)) {
      cat(sprintf("  %s.%s: marked %s, should be %s\n", 
        aspectj_errors$class[i], aspectj_errors$method[i],
        aspectj_errors$doc_value[i], aspectj_errors$expected[i]))
    }
    cat("\n")
  }
  
  if (nrow(bytebuddy_errors) > 0) {
    cat(sprintf("DOC_BYTEBUDDY errors (%d):\n", nrow(bytebuddy_errors)))
    for (i in 1:nrow(bytebuddy_errors)) {
      cat(sprintf("  %s.%s: marked %s, should be %s\n", 
        bytebuddy_errors$class[i], bytebuddy_errors$method[i],
        bytebuddy_errors$doc_value[i], bytebuddy_errors$expected[i]))
    }
    cat("\n")
  }
  
  if (nrow(archunit_errors) > 0) {
    cat(sprintf("DOC_ARCHUNIT errors (%d):\n", nrow(archunit_errors)))
    for (i in 1:nrow(archunit_errors)) {
      cat(sprintf("  %s.%s: marked %s, should be %s\n", 
        archunit_errors$class[i], archunit_errors$method[i],
        archunit_errors$doc_value[i], archunit_errors$expected[i]))
    }
    cat("\n")
  }
  
  if (nrow(wala_errors) > 0) {
    cat(sprintf("DOC_WALA errors (%d):\n", nrow(wala_errors)))
    for (i in 1:nrow(wala_errors)) {
      cat(sprintf("  %s.%s: marked %s, should be %s\n", 
        wala_errors$class[i], wala_errors$method[i],
        wala_errors$doc_value[i], wala_errors$expected[i]))
    }
  }
}

cat("\nDone!\n")
