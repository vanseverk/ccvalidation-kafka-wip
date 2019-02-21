package be;

import org.junit.Test;
import reactor.core.publisher.Flux;

public class FluxMonoTest {

    @Test
    public void bla() {
        Flux<Integer> startFlux = Flux.just(1, 2, 3, 4, 5).share();

        Flux.from(startFlux).subscribe();

        Flux.from(startFlux).filter(i -> i == 2).single().subscribe(System.out::println);
        Flux.from(startFlux).filter(i -> i == 4).single().subscribe(System.out::println);
        Flux.from(startFlux).filter(i -> i == 5).single().subscribe(System.out::println);
    }
}
