package rsstats.roll;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class RollModifierTest {

    // Gives 100% coverage on equals and hashCode methods.
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(RollModifier.class)
                .suppress(Warning.STRICT_INHERITANCE) // Я не хочу финалить equals, т.к. его могут оверрайдить наследники
                .withNonnullFields("description")
                .verify();
    }
}