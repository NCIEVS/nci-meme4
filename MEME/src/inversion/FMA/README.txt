** Note: this program is currently under modification to accommodate the OWL
** structures that occur in FMA4_0, but these modifications are currently
** incomplete, so the program in its current state is likely not to work!

OWL consists of various nested structures that describe the elements of
an ontology, and each particular nested sequence of structures can be used
to express a variety of specific relationships or attributes. For example,
one nesting sequence that occurs in FMA4 is: Class, subClassOf, Restriction,
someValuesFrom, Class, intersectionOf, Restriction, someValuesFrom/hasValue. A
sample instance of this nesting sequence is:

<owl:Class rdf:about="&fma;fma61991">
  <rdfs:label xml:lang="en">Trunk of right middle cerebral artery</rdfs:label>
[...]
  <rdfs:subClassOf>
    <owl:Restriction>
      <owl:onProperty rdf:resource="&fma;attributed_part"/>
      <owl:someValuesFrom>
        <owl:Class>
          <owl:intersectionOf rdf:parseType="Collection">
            <owl:Restriction>
              <owl:onProperty rdf:resource="&fma;related_part"/>
              <owl:someValuesFrom rdf:resource="&fma;fma50369"/>
            </owl:Restriction>
            <owl:Restriction>
              <owl:onProperty rdf:resource="&fma;partition"/>
              <owl:hasValue rdf:datatype="&xsd;string">Partition 2</owl:hasValue>
            </owl:Restriction>
          </owl:intersectionOf>
        </owl:Class>
      </owl:someValuesFrom>
    </owl:Restriction>
  </rdfs:subClassOf>
[...]
</owl:Class>

Here the outermost Restriction is onProperty "attributed_part", to express an
attributed_part relationship. Altogether there are about 156 Object properties
(relationships) and 65 Annotation and Datatype properties (attributes).

In the above example, it's basically saying that fma61991 (Trunk of right
middle cerebral artery) has an attributed_part relationship to fma50369
(Insular part of right middle cerebral artery), and that relationship has a
"partition" relationship-attribute whose value is "Partition 2".

The owl inversion process uses an "OWL to TREF" program that I wrote for
earlier, simpler owl files (originally for the NCI Thesaurus). The above
structure is one that doesn't occur in NCI Thesaurus. The OWL to TREF program
uses the Jena owl parser, without which I'd have to write (and debug) my own
owl parser. The challenge is to modify the OWL to TREF program to do the right
thing with all the owl structures present in FMA4. To work with Jena, the
program (so far - I may have to further modify and add to this to handle all
the cases) uses the following series of nested loops (described below in a
very rough pseudocode) to interpret what Jena produces:

 for each Class:
  for each "equivalentClass" or "superClass" of the class:
   for each "intersectionOf" component:
    for each "unionOf" subcomponent:
     for each "intersectionOf" sub-subcomponent:
      for each "unionOf" sub-sub-subcomponent:
       for each "intersectionOf" sub-sub-sub-subcomponent:
        if this component is a HasValueRestriction, process it;
        if this component is an AllValuesFrom or SomeValuesFrom Restriction, find the rel type from its "onProperty":
        if this component is an anonymous Restriction:
         find the "qualifying rel" (like the "related_part" above pointing to the target of the relationship attribute);
         if this component is an "intersection":
          for each "intersectionOf" subcomponent:
           process it in various ways depending whether it's a HasValue, AllValuesFrom or SomeValuesFrom Restriction;
         if this component is a "union":
          for each "unionOf" subcomponent:
           process it as a "union Restriction"

This program structure is pretty much required by how the Jena parser works
for the various owl structures that occur. I'm taking a "data-driven"
approach: basically, for each kind of owl structure, I find an example
of that structure in the owl and then I trace through the program to
find where each piece of it "emerges" from Jena in the above nested loop
structure, and then I try to set up data structures (variables) to save
all the relevant "pieces" where they appear (e.g., in the above example,
this would include the two concepts being related, the relationship type,
and the rel attribute's name and value, plus a generated RUI value), figure
out how we want to represent that structure, write out the representation,
and finally q/a the results by comparing to how similar elements appear in
our old inversion of FMA3 (which used a simpler, slot-based representation
that was more straightforward to invert).
