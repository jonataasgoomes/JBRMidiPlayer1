public class Utilitario {
    
    public static String obtemTonalidade(boolean maior, byte tonalidade) {
        switch (tonalidade) {
            case -7: return maior ? "Dób Maior" : "Láb Menor";
            case -6: return maior ? "Solb Maior": "Mib Menor";
            case -5: return maior ? "Réb Maior": "Sib Menor";
            case -4: return maior ? "Láb Maior": "Fá Menor";
            case -3: return maior ? "Mib Maior": "Dó Menor";
            case -2: return maior ? "Sib Maior": "Sol Menor";
            case -1: return maior ? "Fá Maior": "Ré Menor";
            case  0: return maior ? "Dó Maior": "Lá Menor";
            case  1: return maior ? "Sol Maior": "Mi Menor";
            case  2: return maior ? "Ré Maior": "Si Menor";
            case  3: return maior ? "Lá Maior": "Fá# Menor";
            case  4: return maior ? "Mi Maior": "Dó# Menor";
            case  5: return maior ? "Si Maior": "Sol# Menor";
            case  6: return maior ? "Fá# Maior": "Ré# Menor";
            case  7: return maior ? "Dó# Maior": "Lá# Menor";
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
