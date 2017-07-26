OWLtoTREF is a Java program that allows a terminology represented in
an OWL model to be written to TREF format (e.g., for submission to
the UMLS).

The OWLtoTREF program takes two optional arguments:
 -h: print a help message
 -c <config-filename>: specify the configuration file (default: "tref.config")

The configuration file is a java Properties file with lines of the form
"key=value"; lines beginning with "#" are comments.

The configuration must contain a "inputURI" property whose value is the
URI of the OWL file to be read as input, e.g. "file:Thesaurus.owl".

All other properties in the configuration file specify the output files
to be written and what to write to them.

To specify an output file, give it a handle and a filename by means of
a property specification as follows:

  handle.name=filename

For example,

  MRCONSO.name=MRCONSO.RRF

specifies a file handle "MRCONSO" whose corresponding output filename is
"MRCONSO.RRF".

Then, define the field specifications for each defined file handle, as
follows:

  handle.fields=field1|field2|...|fieldN

where each "field#" item defines the name of a field of the lines of
the given file handle.  The "|" character must be used as the field
separator in this specification.  A "field#" item may have multiple
components separated by an underscore ("_"), in which case these
components can be filled independently during output processing; the
components will then be run together in the output field in the
order specified.

For example,

  MRCONSO.fields=AUI|STR|CUI|DUI|SAB_TTY

specifies that the file for the MRCONSO file handle will have 5
fields; the first four fields are called AUI, STR, CUI, and DUI.
The 5th field will consist of two components, SAB and TTY, run
together.  Thus, if the SAB component is filled with "Source"
and the TTY component is filled with "PT", then the 5th field
will be "SourcePT".  If you wanted the 5th field to say
"Source/PT" instead of "SourcePT", you would have to either
fill SAB with "Source/" or fill TTY with "/PT".

Finally, each file handle may be assigned an arbitrary number of
output "items" to specify content to be written to the file.
Each item is identified by a number (or actually any name other
than "name" or "fields").  Each item must have a "key" property
specified as follows:

  handle.item.key=<property-name>

or a list of "key" properties, as follows:

  handle.item.key=<property-name1>,<property-name2>,...,<property-nameN>

For example,

  MRCONSO.1.key=Synonym

specifies that item 1 for the MRCONSO file handle is triggered by
instances of the Synonym property.  If multiple property names are
specified as keys for the same item, then any of those properties
will trigger the item.

A special key value, "*superclasses", indicates that the item
should be triggered for each of the superclasses of a given
class.

Then, each item can have additional properties to specify how to
fill the file's various fields, as follows:

  handle.item.<field-name(s)>=<value-specification>

where <value-specification> can take any of the following forms:

  const|<const-value>
  field|<property-name>
  regexp|<property-name>|<regular-expression>
  targetfield|<property-name>
  superclassfield|<property-name>

The <const-value> and <property-name> items may optionally have
the value "*key", in which case they are replaced with the value
of the current key field that triggered the output item.
