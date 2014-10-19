package nars;

import java.util.List;
import nars.core.Memory;
import nars.entity.Task;
import nars.language.Term;
import nars.prolog.InvalidTheoryException;
import nars.prolog.Prolog;
import nars.prolog.Theory;


/**
 * Executes Prolog code
 * Access Prolog from NARS
 * @author irc SquareOfTwo | github PtrMan
 * 
 * Example:
        < (&/, (^prolog, "exec", "database(Key, Result).", "Result", $ra, "Key", $keyA) )    =/> <(*, $keyA, $ra) --> executed > >.
        <(*, 0, #a) --> executed >!

   Should return: 
        <(*, 0, "test") --> executed>
 */
public class PrologTheoryStringOperator extends nars.operator.Operator {
    private final PrologContext context;
    
    static public class ConversionFailedException extends RuntimeException {
        public ConversionFailedException() {
            super("Conversion Failed");
        }
    }
   
    public PrologTheoryStringOperator(PrologContext context) {
        super("^prologTheoryString");
        this.context = context;
    }

    @Override
    protected List<Task> execute(nars.operator.Operation operation, Term[] args, Memory memory) {
        if (args.length != 3) {
            return null;
        }
       
        Term prologInterpreterKey = args[0];
        String theoryName = args[1].name().toString(); // ASK< correct? >
        String theoryContent = PrologQueryOperator.getStringOfTerm(args[2]);
        
        // NOTE< throws exception, we just don't catch it and let nars handle it >
        Prolog prologInterpreter = PrologTheoryUtility.getOrCreatePrologContext(prologInterpreterKey, context);
        
        // TODO< map somehow the theory name to the theory itself and reload if overwritten >
        // NOTE< theoryName is not used >
        // NOTE< theory is not cached >
        try {
            prologInterpreter.addTheory(new Theory(theoryContent));
        }
        catch (InvalidTheoryException exception) {
            // TODO< report error >
            return null;
        }
        
        memory.emit(Prolog.class, prologInterpreterKey + "=" + theoryContent );

        return null;
    }
   
   
}
