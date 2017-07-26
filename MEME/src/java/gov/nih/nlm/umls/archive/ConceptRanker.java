/*****************************************************************************
 * Package: gov.nih.nlm.umls.archive
 * Object:  ConceptRanker.java
 *
 * CHANGES:
 *   08/02/2007: First Version
 ****************************************************************************/

package gov.nih.nlm.umls.archive;

import gov.nih.nlm.mms.CuiSuiAuiList;
import gov.nih.nlm.mms.MetamorphoSysInputStream;
import gov.nih.nlm.mms.OriginalMRMetamorphoSysInputStream;
import gov.nih.nlm.mms.RichMRMetamorphoSysInputStream;
import gov.nih.nlm.mms.UserConfiguration;
import gov.nih.nlm.mms.filters.PrecedenceFilter;
import gov.nih.nlm.umls.meta.Atom;
import gov.nih.nlm.umls.meta.Concept;
import gov.nih.nlm.umls.meta.MetaDescription;
import gov.nih.nlm.umls.meta.MetaDescriptionInitializer;
import gov.nih.nlm.umls.meta.Source;
import gov.nih.nlm.umls.meta.SourceTermType;
import gov.nih.nlm.umls.meta.Suppressible;
import gov.nih.nlm.umls.meta.TermType;
import gov.nih.nlm.umls.meta.impl.MetaDescriptionImpl;
import gov.nih.nlm.util.SystemToolkit;
import gov.nih.nlm.util.UTF8OutputStreamWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConceptRanker {
    public ConceptRanker() {
    }

    public void transform(String dir, boolean rich_input, boolean rich_output)
            throws Exception {

        // Confirm directory is usable

        // Initialize Metadata
        if (rich_input) {
            new MetaDescriptionInitializer().initialize(
                    new File(dir,"MRDOC.RRF"), new File(dir, "MRSAB.RRF"), null);
        } else {
            new MetaDescriptionInitializer().initialize(
                    null, new File(dir, "MRSAB"), null);
        }
        
        System.setProperty("line.separator", "\n");

        // Configure Precedence filter
        PrecedenceFilter filter = new PrecedenceFilter();
        Properties input_props = new Properties();
        input_props.load(this.getClass().getClassLoader().getResourceAsStream(
                "config/mmsys.a.prop"));
        UserConfiguration user_config = new UserConfiguration(input_props);
        List prec_list = new ArrayList();
        // open dir/MRRANK.RRF
        // For each line, look up the correct "Source" and "TermType" objects
        // construct a SourceTermType object and add it to the list.
        // I believe the "first" termgroup in the list is the highest rank.
        // Since MRRANK is reverse-sorted by rank, it has the same property
        final String[][] mrrank = SystemToolkit.readFieldedFile(new File(dir,
                rich_input ? "MRRANK.RRF" : "MRRANK"), "|");
        for (int i = 0; i < mrrank.length; i++) {
            SourceTermType tg = new SourceTermType();
            tg.setSource(Source.getSource(mrrank[i][1]));
            final TermType tty = TermType.getTermType(mrrank[i][2]);
            if (tty == null) {
              //
              // We have no MRDOC, so fake it
              //
              MetaDescription[] mdsc_array = new MetaDescription[1];
              mdsc_array[0] = new MetaDescriptionImpl("TTY|" +
                                                      mrrank[i][2] + "|expanded_form||");

              TermType.initialize(mdsc_array);
            }
            tg.setTermType(TermType.getTermType(mrrank[i][2]));
            tg.setSuppress(Suppressible.getSuppressible(mrrank[i][3]));
            prec_list.add((Object) tg);
        }
        // configure user config
        filter.setConfiguration(user_config);
        user_config.setPrecedence(prec_list);

        // Open input/output handlers
        MetamorphoSysInputStream in = null;
        if (rich_input) {
            in = new RichMRMetamorphoSysInputStream();
        } else {
            in = new OriginalMRMetamorphoSysInputStream();
        }
        in.setEfficientMode(true);
        in.beginInitializeCuiList();
        in.setCuiList(new CuiSuiAuiList());
        in.open(new String[] { dir });

        PrintWriter outCON = null;
        PrintWriter outSO = null;
        if (rich_output) {
            outCON = new PrintWriter(new BufferedWriter(
                    new UTF8OutputStreamWriter(new FileOutputStream(new File(
                            dir, "MRCONSO.RRF.out")), false), 128 * 1024));
        } else {
            outCON = new PrintWriter(new BufferedWriter(
                    new UTF8OutputStreamWriter(new FileOutputStream(new File(
                            dir, "MRCON.out")), false), 128 * 1024));
            outSO = new PrintWriter(new BufferedWriter(
                    new UTF8OutputStreamWriter(new FileOutputStream(new File(
                            dir, "MRSO.out")), false), 128 * 1024));
        }

        // Loop through concepts.
        Concept c = null;
        while ((c = in.readConcept()) != null) {
            filter.applyChanges(c);
            if (rich_output) {
                writeMRCONSO(c, outCON);
            } else {
                writeMRCON(c, outCON);
                writeMRSO(c, outSO);
            }
        }
        in.close();
        outCON.close();
        if (!rich_output) {
            outSO.close();
        }
    }

    /**
     * Writes <code>MRCONSO.RRF</code> entries for a {@link Concept}
     * 
     * @param concept
     *            the {@link Concept} to write
     * @throws IOException
     *             if anything goes wrong
     */
    protected void writeMRCONSO(Concept concept, PrintWriter out)
            throws IOException {

        // for Comparable ORF, we want versionless SABs
        boolean versioned_output = false;

        //
        // Iterate through atoms
        //
        final Atom[] atoms = concept.getAtoms();
        final List mrconso_lines = new ArrayList(atoms.length);
        for (int i = 0; i < atoms.length; i++) {

            //
            // Prepare MRCONSO.RRF line
            //
            final Atom atom = atoms[i];
            final StringBuffer sb = new StringBuffer(150);
            sb.append(atom.getConcept().getCui()).append("|");
            sb.append(atom.getLat().getAbbreviation()).append("|");
            sb.append(atom.getTs().getAbbreviation()).append("|");
            sb.append(atom.getLui()).append("|");
            sb.append(atom.getStt().getAbbreviation()).append("|");
            sb.append(atom.getSui()).append("|");
            sb.append((atom.hasPreferredAtomIdentifier()) ? "Y" : "N").append(
                    "|");
            sb.append(
                    (isEmptyValue(atom.getAui())) ? getNullValue() : atom
                            .getAui()).append("|");
            sb.append(
                    (isEmptyValue(atom.getSaui())) ? getNullValue() : atom
                            .getSaui()).append("|");
            sb.append(
                    (isEmptyValue(atom.getScui())) ? getNullValue() : atom
                            .getScui()).append("|");
            sb.append(
                    (isEmptyValue(atom.getSdui())) ? getNullValue() : atom
                            .getSdui()).append("|");
            if (versioned_output)
                sb.append(atom.getSource().getVersionedSourceAbbreviation());
            else
                sb.append(atom.getSource().getRootSourceAbbreviation());
            sb.append("|");
            sb.append(atom.getTty().getAbbreviation()).append("|");
            sb.append(atom.getCode()).append("|");
            sb.append(atom.getStr()).append("|");
            sb.append(atom.getSource().getSrl()).append("|");
            sb.append(
                    (atom.getSuppress() != null && !isEmptyValue(atom
                            .getSuppress().getAbbreviation())) ? atom
                            .getSuppress().getAbbreviation() : getNullValue())
                    .append("|");
            sb.append(
                    (atom.getCvf() != null) ? atom.getCvf().toString()
                            : getNullValue()).append("|");

            //
            // Add line to list
            //
            mrconso_lines.add(sb.toString());
        }
        //
        // Sort (unique) lines and write to MRCONSO.RRF
        //
        Collections.sort(mrconso_lines);
        String prev_line = "";
        for (int i = 0; i < mrconso_lines.size(); i++) {
            final String this_line = mrconso_lines.get(i).toString();
            if (!this_line.equals(prev_line))
                out.println(this_line);
            prev_line = this_line;
        }
    }

    /**
     * Writes <code>MRSO</code> entries for a {@link Concept}
     * 
     * @param concept
     *            the {@link Concept} to write
     * @throws IOException
     *             if anything goes wrong
     */
    protected void writeMRSO(Concept concept, PrintWriter out)
            throws IOException {

        // for Comparable ORF, we want versionless SABs
        boolean versioned_output = false;

        //
        // Iterate through atoms in concept
        //
        final Atom[] atoms = concept.getAtoms();
        final List mrso_lines = new ArrayList();
        for (int i = 0; i < atoms.length; i++) {

            final Atom atom = atoms[i];

            //
            // Prepare MRSO line
            //
            final StringBuffer sb = new StringBuffer(150);
            sb.append(atom.getConcept().getCui()).append("|");
            sb.append(atom.getLui()).append("|");
            sb.append(atom.getSui()).append("|");
            if (versioned_output)
                sb.append(atom.getSource().getVersionedSourceAbbreviation());
            else
                sb.append(atom.getSource().getRootSourceAbbreviation());
            sb.append("|");
            sb.append(atom.getTty().getAbbreviation()).append("|");
            sb.append(atom.getCode()).append("|");
            sb.append(atom.getSource().getSrl()).append("|");

            //
            // Add line to list
            //
            mrso_lines.add(sb.toString());
        }

        //
        // Sort lines for this concept and write them out
        //
        Collections.sort(mrso_lines);
        String prev_line = null;
        for (int i = 0; i < atoms.length; i++) {
            String this_line = (String) mrso_lines.get(i);
            if (!this_line.equals(prev_line))
                out.println(this_line);
            prev_line = this_line;
        }

    }

    /**
     * Writes <code>MRCON</code> entries for a {@link Concept}
     * 
     * @param concept
     *            the {@link Concept} to write
     * @throws IOException
     *             if anything goes wrong
     */
    protected void writeMRCON(Concept concept, PrintWriter out)
            throws IOException {

        //
        // Obtain atoms and assign LRL field
        //
        final Atom[] atoms = concept.getAtoms();
        final List mrcon_lines = new ArrayList();
        final Map lrl_hm = assignLrl(atoms);

        //
        // Iterate through atoms
        //
        for (int i = 0; i < atoms.length; i++) {

            final Atom atom = atoms[i];

            //
            // Prepare MRCON line
            //
            final StringBuffer sb = new StringBuffer(200);
            sb.append(atom.getConcept().getCui()).append("|");
            sb.append(atom.getLat().getAbbreviation()).append("|");
            if (isLuiSuppressible(atom.getLui(), atoms))
                sb.append(atom.getTs().getAbbreviation().toLowerCase());
            else
                sb.append(atom.getTs().getAbbreviation());
            sb.append("|");
            sb.append(atom.getLui()).append("|");
            sb.append(atom.getStt().getAbbreviation()).append("|");
            sb.append(atom.getSui()).append("|");
            sb.append(atom.getStr()).append("|");
            sb.append(
                    lrl_hm
                            .get(atom.getConcept().getCui() + ":"
                                    + atom.getSui())).append("|");

            //
            // Add line to list
            //
            mrcon_lines.add(sb.toString());
        }

        //
        // Sort (unique) lines and write to MRCON
        //
        Collections.sort(mrcon_lines);
        String this_line, prev_line = "";
        for (int i = 0; i < mrcon_lines.size(); i++) {
            this_line = (String) mrcon_lines.get(i);
            if (!this_line.equals(prev_line))
                out.println(this_line);
            prev_line = this_line;
        }
    }

    /**
     * Returns <code>true</code> if the specified LUI is suppressible.
     * 
     * @param lui
     *            the LUI value to check
     * @param atoms
     *            the {@link Atom}s
     * @return <code>true</code> if the LUI is suppressible,
     *         <code>false</code> otherwise
     */
    protected boolean isLuiSuppressible(String lui, Atom[] atoms) {

        //
        // Iterate through atoms
        //
        for (int i = 0; i < atoms.length; i++) {
            if (lui.equals(atoms[i].getLui())) {
                //
                // If any atom is not suppressible, the LUI is not suppressible
                //
                if (!atoms[i].isSuppressible())
                    return false;
            }
        }

        //
        // If all atoms are suppressible, LUI is suppressible
        //
        return true;
    }

    /**
     * Assigns the LRL value.
     * 
     * @param atoms
     *            the {@link Atom}s to be assigned
     * @return {@link Map} of cui:sui tuple mapped to lrl
     */
    protected Map assignLrl(Atom[] atoms) {

        //
        // Iterate through atoms
        //
        final Map lrl_hm = new HashMap();
        for (int i = 0; i < atoms.length; i++) {

            //
            // Prepare CUI,SUI tuple as key
            //
            final String cui_sui = atoms[i].getConcept().getCui() + ":"
                    + atoms[i].getSui();

            //
            // If current SRL is lower than LRL for this CUI,SUI, update it to
            // new value
            //
            if (lrl_hm.containsKey(cui_sui)) {
                if (atoms[i].getSource().getSrl().compareTo(
                        (String) lrl_hm.get(cui_sui)) < 0)
                    lrl_hm.put(cui_sui, atoms[i].getSource().getSrl());
            }

            //
            // If no LRL has been assigned for this CUI,SUI, use the current SRL
            else
                lrl_hm.put(cui_sui, atoms[i].getSource().getSrl());

        }
        return lrl_hm;
    }

    /**
     * Indicates whether or not the specified token is null or contains a null
     * value as defined by being either zero length or a value equal to the null
     * value.
     */
    public boolean isEmptyValue(String token) {
        final String null_value = "";
        return token == null || token.length() == 0 ||
        // non-default null value
                token.equals(null_value);
    }

    /**
     * Returns the null value. This is needed if release.dat specifies some
     * non-standard null value char sequence.
     */
    public String getNullValue() {
        return "";
    }

    public static void main(String[] args) {
        try {

            ConceptRanker ranker = new ConceptRanker();

            // Extract orf/rrf from parameters
            boolean rich_input = true;
            boolean rich_output = true;
            int arg = 0;
            while (args[arg].equals("-i") || args[arg].equals("-o")) {
                if (args[arg].equals("-i")) {
                    arg++;
                    if (args[arg].equals("ORF")) {
                        rich_input = false;
                    } else if (args[arg].equals("RRF")) {
                        rich_input = true;
                    }
                    arg++;
                } else if (args[arg].equals("-o")) {
                    arg++;
                    if (args[arg].equals("ORF")) {
                        rich_output = false;
                    } else if (args[arg].equals("RRF")) {
                        rich_output = true;
                    }
                    arg++;
                }
            }

            // extract dir from parameters
            String dir = args[arg];

            ranker.transform(dir, rich_input, rich_output);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
