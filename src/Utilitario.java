public class Utilitario {
    
    public static String obtemTonalidade(boolean maior, byte tonalidade) {
        if (maior) {
            switch (tonalidade) {
                case -7: return "Dób Maior" ;
                case -6: return "Solb Maior";
                case -5: return "Réb Maior";
                case -4: return "Láb Maior";
                case -3: return "Mib Maior";
                case -2: return "Sib Maior";
                case -1: return "Fá Maior";
                case  0: return "Dó Maior";
                case  1: return "Sol Maior";
                case  2: return "Ré Maior";
                case  3: return "Lá Maior";
                case  4: return "Mi Maior";
                case  5: return "Si Maior";
                case  6: return "Fá# Maior";
                case  7: return "Dó# Maior";
            }
        } else {
            switch (tonalidade) {
                case -7: return "Láb Menor";
                case -6: return "Mib Menor";
                case -5: return "Sib Menor";
                case -4: return "Fá Menor";
                case -3: return "Dó Menor";
                case -2: return "Sol Menor";
                case -1: return "Ré Menor";
                case  0: return "Lá Menor";
                case  1: return "Mi Menor";
                case  2: return "Si Menor";
                case  3: return "Fá# Menor";
                case  4: return "Dó# Menor";
                case  5: return "Sol# Menor";
                case  6: return "Ré# Menor";
                case  7: return "Lá# Menor";
            }
        }
        return "?";
    }
    
    public static String obtemFormulaCompasso(byte numerador, byte denominadorPotencia) {
        
        if (denominadorPotencia < 0 || numerador < 0 || denominadorPotencia > 30) {
            return "4/4";
        }
        
        return String.format("%d/%d", numerador, (int)Math.pow(2, denominadorPotencia));
    }
}
