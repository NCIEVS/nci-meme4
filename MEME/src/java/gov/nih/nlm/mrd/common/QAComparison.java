/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  QAComparison
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

/**
 * Represents the comparison of two {@link QAResult}s.
 * 
 * @author TTN
 */
public class QAComparison extends QAResult {

	/**
	 * The comparison count.
	 */
	private long comparison_count;

	/**
	 * Returns the comparison count.
	 * 
	 * @return the comparison count
	 */
	public long getComparisonCount() {
		return comparison_count;
	}

	/**
	 * Sets the comparison count.
	 * 
	 * @param comparison_count
	 *            the comparison count
	 */
	public void setComparisonCount(long comparison_count) {
		this.comparison_count = comparison_count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final QAComparison other = (QAComparison) obj;
		if (getDiffCount() != other.getDiffCount())
			return false;
		return true;
	}

	/**
	 * @return the diffCount
	 */
	public long getDiffCount() {
		return count - comparison_count;
	}

	/* (non-Javadoc)
	 * @see gov.nih.nlm.mrd.common.QAResult#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + (int)getDiffCount();
	}

}