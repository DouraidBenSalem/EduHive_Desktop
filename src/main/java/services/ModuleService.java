package Services;

import Entities.Module;
import java.util.List;

public interface ModuleService {
    List<Module> getAllModules();
    Module getModuleById(int id);
    void addModule(Module module);
    void updateModule(Module module);
    void deleteModule(int id);
}